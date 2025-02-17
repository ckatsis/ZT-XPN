/**
 * ZT-XPN: An end-to-end Zero-Trust Architecture for Next Generation 
 * Programmable Networks
 * 
 * Authors:  Charalampos Katsis  (ckatsis@purdue.edu)
 *           Elisa Bertino       (bertino@purdue.edu)
 * =================================================================
 */


package org.p4sdn.app;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.onlab.osgi.ServiceDirectory;
import org.onlab.util.KryoNamespace;
import org.onosproject.net.DeviceId;
import org.onosproject.net.behaviour.NextGroup;
import org.onosproject.net.behaviour.Pipeliner;
import org.onosproject.net.behaviour.PipelinerContext;
import org.onosproject.net.driver.AbstractHandlerBehaviour;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleOperations;
import org.onosproject.net.flow.FlowRuleOperationsContext;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flow.criteria.Criterion;
import org.onosproject.net.flow.instructions.Instruction;
import org.onosproject.net.flowobjective.FilteringObjective;
import org.onosproject.net.flowobjective.FlowObjectiveStore;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.onosproject.net.flowobjective.NextObjective;
import org.onosproject.net.flowobjective.Objective;
import org.onosproject.net.flowobjective.ObjectiveError;
import org.onosproject.net.pi.model.PiTableId;
import org.onosproject.net.pi.model.PiPipelineInterpreter.PiInterpreterException;
import org.onosproject.net.pi.runtime.PiAction;
import org.onosproject.net.pi.runtime.PiActionParam;
import org.onosproject.store.serializers.KryoNamespaces;
import org.p4sdn.app.flow.ExtendedInstruction;
import org.p4sdn.app.flow.StatefulProcessingInstruction;
import org.p4sdn.app.pipeline.Pipeline;
import org.p4sdn.app.pipeline.PipelineRole;
import org.slf4j.Logger;

import com.google.common.cache.RemovalNotification;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;

import static java.lang.String.format;
import static org.onlab.util.ImmutableByteSequence.copyFrom;
import static org.onosproject.net.flowobjective.Objective.Operation.ADD;

import static org.slf4j.LoggerFactory.getLogger;


public class PipelinerHandler extends AbstractHandlerBehaviour implements Pipeliner {

    private final Logger log = getLogger(getClass());

    private ServiceDirectory serviceDirectory;
    private FlowRuleService flowRuleService;
    private FlowObjectiveStore flowObjectiveStore;
    private DeviceId deviceId;

    private KryoNamespace appKryo = new KryoNamespace.Builder()
            .register(KryoNamespaces.API)
            .register(SingleGroup.class)
            .build("DefaultSingleTablePipeline");
    
    // Fast path for the installation. If we don't find the nextobjective in
    // the cache, as fallback mechanism we will try to retrieve the treatments
    // from the store. It is safe to use this cache for the addition, while it
    // should be avoided for the removal. This cache from Guava does not offer
    // thread-safe read (the read does not take the lock).
    private Cache<Integer, NextObjective> pendingAddNext = CacheBuilder.newBuilder()
            .expireAfterWrite(20, TimeUnit.SECONDS)
            .removalListener((RemovalNotification<Integer, NextObjective> notification) -> {
                if (notification.getCause() == RemovalCause.EXPIRED) {
                    notification.getValue().context()
                            .ifPresent(c -> c.onError(notification.getValue(),
                                                      ObjectiveError.FLOWINSTALLATIONFAILED));
                }
            }).build();

    @Override
    public void init(DeviceId deviceId, PipelinerContext context) {
        this.serviceDirectory = context.directory();
        this.deviceId = deviceId;

        flowRuleService = serviceDirectory.get(FlowRuleService.class);
        flowObjectiveStore = serviceDirectory.get(FlowObjectiveStore.class);
    }

    @Override
    public void filter(FilteringObjective filter) {
        TrafficTreatment.Builder actions;
        switch (filter.type()) {
            case PERMIT:
                actions = (filter.meta() == null) ?
                        DefaultTrafficTreatment.builder().punt() :
                        DefaultTrafficTreatment.builder(filter.meta());
                break;
            case DENY:
                actions = (filter.meta() == null) ?
                        DefaultTrafficTreatment.builder() :
                        DefaultTrafficTreatment.builder(filter.meta());
                actions.drop();
                break;
            default:
                log.warn("Unknown filter type: {}", filter.type());
                actions = DefaultTrafficTreatment.builder().drop();
        }

        TrafficSelector.Builder selector = DefaultTrafficSelector.builder();

        filter.conditions().forEach(selector::add);

        if (filter.key() != null) {
            selector.add(filter.key());
        }

        FlowRule.Builder ruleBuilder = DefaultFlowRule.builder()
                .forDevice(deviceId)
                .withSelector(selector.build())
                .withTreatment(actions.build())
                .fromApp(filter.appId())
                .withPriority(filter.priority());

        if (filter.permanent()) {
            ruleBuilder.makePermanent();
        } else {
            ruleBuilder.makeTemporary(filter.timeout());
        }
        installObjective(ruleBuilder, filter);
    }

    private void installObjective(FlowRule.Builder ruleBuilder, Objective objective) {
        FlowRuleOperations.Builder flowBuilder = FlowRuleOperations.builder();
        switch (objective.op()) {
            case ADD:
                flowBuilder.add(ruleBuilder.build());
                break;
            case REMOVE:
                flowBuilder.remove(ruleBuilder.build());
                break;
            default:
                log.warn("Unknown operation {}", objective.op());
        }

        flowRuleService.apply(flowBuilder.build(new FlowRuleOperationsContext() {
            @Override
            public void onSuccess(FlowRuleOperations ops) {
                objective.context().ifPresent(context -> context.onSuccess(objective));
            }

            @Override
            public void onError(FlowRuleOperations ops) {
                objective.context()
                        .ifPresent(context -> context.onError(objective, ObjectiveError.FLOWINSTALLATIONFAILED));
            }
        }));
    }

    private TrafficSelector filterUnsupportedMatchFields(ForwardingObjective fwd, Pipeline pipeline) {
        // Collect only the supported criteria
        List<Criterion> supportedCriteria = fwd.selector().criteria().stream()
                .filter(pipeline::isSupportedCriterion)
                .collect(Collectors.toList());
        
        // Build a new TrafficSelector with the supported criteria
        TrafficSelector.Builder selectorBuilder = DefaultTrafficSelector.builder();
        supportedCriteria.forEach(selectorBuilder::add);

        return selectorBuilder.build();
    }

    private boolean isStatefulTreatmet(TrafficTreatment treatment) {
        Instruction instruction = treatment.allInstructions().get(0);

        if (instruction instanceof ExtendedInstruction) {
            ExtendedInstruction ei = (ExtendedInstruction) instruction;

            if (ei.extendedType() == ExtendedInstruction.ExtendedType.STATEFUL_PROCESSING) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void forward(ForwardingObjective fwd) {
        // 1. Get pipeline for this switch.
        // Pipeline sw_pipeline = PolicyOrchistrator.getSwitchPipeline("sw_0");
        Pipeline sw_pipeline = PolicyOrchistrator.getSwitchPipeline(deviceId.toString());
        // TrafficSelector requestedSelector = fwd.selector(); // Received selector
        TrafficSelector filteredSelector = filterUnsupportedMatchFields(fwd, sw_pipeline);
        TrafficTreatment policyTreatment = fwd.treatment();

        if (fwd.treatment() != null) {
            // log.info("Installing objective for the P4 program");
            FlowRule.Builder ruleBuilder = DefaultFlowRule.builder();

            if (isStatefulTreatmet(policyTreatment)) {
                Instruction instruction = policyTreatment.allInstructions().get(0);
                ExtendedInstruction ei = (ExtendedInstruction) instruction;
                StatefulProcessingInstruction stateInstruction = (StatefulProcessingInstruction) ei;
                PiTableId tableId = PipelineIntepreterHandler.tableMap.get(0);
                PiAction.Builder statefulAction = PiAction.builder()
                                .withId(PipelineIntepreterHandler.packetStatefulProcessing);
                
                if (stateInstruction.isKeepState()) {
                    statefulAction.withParameter(new PiActionParam(PipelineIntepreterHandler.isStatefulParam, copyFrom(1)))
                                    .withParameter(new PiActionParam(PipelineIntepreterHandler.stateIndexParam, 
                                            copyFrom(stateInstruction.getStateId())));
                } else {
                    statefulAction.withParameter(new PiActionParam(PipelineIntepreterHandler.isStatefulParam, copyFrom(0)))
                                    .withParameter(new PiActionParam(PipelineIntepreterHandler.stateIndexParam, copyFrom(0)));   
                }

                if (stateInstruction.isDependent()) {
                    statefulAction.withParameter(new PiActionParam(PipelineIntepreterHandler.isDependentFlowParam, copyFrom(1)))
                                    .withParameter(new PiActionParam(PipelineIntepreterHandler.dependencyStateIndexParam, 
                                            copyFrom(stateInstruction.getDependencyStateId())));
                } else {
                    statefulAction.withParameter(new PiActionParam(PipelineIntepreterHandler.isDependentFlowParam, copyFrom(0)))
                                    .withParameter(new PiActionParam(PipelineIntepreterHandler.dependencyStateIndexParam, 
                                        copyFrom(0)));
                }
    
                statefulAction.withParameter(new PiActionParam(
                        PipelineIntepreterHandler.outPortParam, copyFrom(stateInstruction.getOutputPort())));
                

                ruleBuilder
                        .forDevice(deviceId)
                        .forTable(tableId)
                        .withSelector(filteredSelector)
                        .fromApp(fwd.appId())
                        .withPriority(fwd.priority())
                        .withTreatment(DefaultTrafficTreatment.builder()
                                .piTableAction(statefulAction.build())
                                .build());
                
            } else {
                PiTableId tableId;

                if (sw_pipeline.getPipelineRole() == PipelineRole.ACL) {
                    tableId = PipelineIntepreterHandler.tableMap.get(0);
                } else {
                    tableId = PipelineIntepreterHandler.tableMap.get(1);
                }

            // Deal with SPECIFIC and VERSATILE in the same manner.
                ruleBuilder
                        .forDevice(deviceId)
                        .forTable(tableId)
                        .withSelector(filteredSelector)
                        .fromApp(fwd.appId())
                        .withPriority(fwd.priority())
                        .withTreatment(fwd.treatment());
            }
            
            if (fwd.permanent()) {
                ruleBuilder.makePermanent();
            } else {
                ruleBuilder.makeTemporary(fwd.timeout());
            }
            installObjective(ruleBuilder, fwd);

        } else {
            log.info("No treatment specified for the P4 program");
            NextObjective nextObjective;
            NextGroup next;
            TrafficTreatment treatment;
            if (fwd.op() == ADD) {
                // Give a try to the cache. Doing an operation
                // on the store seems to be very expensive.
                nextObjective = pendingAddNext.getIfPresent(fwd.nextId());
                // If the next objective is not present
                // We will try with the store
                if (nextObjective == null) {
                    next = flowObjectiveStore.getNextGroup(fwd.nextId());
                    // We verify that next was in the store and then de-serialize
                    // the treatment in order to re-build the flow rule.
                    if (next == null) {
                        fwd.context().ifPresent(c -> c.onError(fwd, ObjectiveError.GROUPMISSING));
                        return;
                    }
                    treatment = appKryo.deserialize(next.data());
                } else {
                    pendingAddNext.invalidate(fwd.nextId());
                    treatment = getTreatment(nextObjective);
                    if (treatment == null) {
                        fwd.context().ifPresent(c -> c.onError(fwd, ObjectiveError.UNSUPPORTED));
                        return;
                    }
                }
            } else {
                // We get the NextGroup from the remove operation.
                // Doing an operation on the store seems to be very expensive.
                next = flowObjectiveStore.getNextGroup(fwd.nextId());
                treatment = (next != null) ? appKryo.deserialize(next.data()) : null;
            }
            // If the treatment is null we cannot re-build the original flow
            if (treatment == null)  {
                fwd.context().ifPresent(c -> c.onError(fwd, ObjectiveError.GROUPMISSING));
                return;
            }
            // Finally we build the flow rule and push to the flow rule subsystem.
            FlowRule.Builder ruleBuilder = DefaultFlowRule.builder()
                    .forDevice(deviceId)
                    .withSelector(filteredSelector)
                    .fromApp(fwd.appId())
                    .withPriority(fwd.priority())
                    .withTreatment(treatment);
            if (fwd.permanent()) {
                ruleBuilder.makePermanent();
            } else {
                ruleBuilder.makeTemporary(fwd.timeout());
            }
            installObjective(ruleBuilder, fwd);
        }
    }

    @Override
    public void next(NextObjective nextObjective) {
        switch (nextObjective.op()) {
            case ADD:
                // Check next objective
                TrafficTreatment treatment = getTreatment(nextObjective);
                if (treatment == null) {
                    // unsupported next objective
                    nextObjective.context().ifPresent(context -> context.onError(nextObjective,
                                                                                 ObjectiveError.UNSUPPORTED));
                    return;
                }
                // We insert the value in the cache
                pendingAddNext.put(nextObjective.id(), nextObjective);

                // Then in the store, this will unblock the queued fwd obj
                flowObjectiveStore.putNextGroup(
                        nextObjective.id(),
                        new SingleGroup(treatment)
                );
                break;
            case REMOVE:
                NextGroup next = flowObjectiveStore.removeNextGroup(nextObjective.id());
                if (next == null) {
                    nextObjective.context().ifPresent(context -> context.onError(nextObjective,
                                                                                 ObjectiveError.GROUPMISSING));
                    return;
                }
                break;
            default:
                log.warn("Unsupported operation {}", nextObjective.op());
        }
        nextObjective.context().ifPresent(context -> context.onSuccess(nextObjective));
    }

    @Override
    public List<String> getNextMappings(NextGroup nextGroup) {
        // Default single table pipeline does not use nextObjectives or groups
        return Collections.emptyList();
    }


    /**
     * Gets traffic treatment from a next objective.
     * Merge traffic treatments from next objective if the next objective is
     * BROADCAST type and contains multiple traffic treatments.
     * Returns first treatment from next objective if the next objective is
     * SIMPLE type and it contains only one treatment.
     *
     * @param nextObjective the next objective
     * @return the treatment from next objective; null if not supported
     */
    private TrafficTreatment getTreatment(NextObjective nextObjective) {
        Collection<TrafficTreatment> treatments = nextObjective.next();
        switch (nextObjective.type()) {
            case SIMPLE:
                if (treatments.size() != 1) {
                    log.error("Next Objectives of type SIMPLE should have only " +
                                      "one traffic treatment. NexObjective: {}",
                              nextObjective.toString());
                    return null;
                }
                return treatments.iterator().next();
            case BROADCAST:
                TrafficTreatment.Builder builder = DefaultTrafficTreatment.builder();
                treatments.forEach(builder::addTreatment);
                return builder.build();
            default:
                log.error("Unsupported next objective type {}.", nextObjective.type());
                return null;
        }
    }


    private class SingleGroup implements NextGroup {

        private TrafficTreatment nextActions;

        SingleGroup(TrafficTreatment next) {
            this.nextActions = next;
        }

        @Override
        public byte[] data() {
            return appKryo.serialize(nextActions);
        }

        public TrafficTreatment treatment() {
            return nextActions;
        }
    }
    
}
