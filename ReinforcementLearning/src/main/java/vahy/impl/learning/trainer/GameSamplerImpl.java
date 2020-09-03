package vahy.impl.learning.trainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.episode.EpisodeResults;
import vahy.api.episode.EpisodeResultsFactory;
import vahy.api.episode.GameSampler;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.episode.PolicyCategory;
import vahy.api.episode.PolicyCategoryInfo;
import vahy.api.episode.PolicyIdTranslationMap;
import vahy.api.episode.PolicyShuffleStrategy;
import vahy.api.episode.RegisteredPolicy;
import vahy.api.episode.StateWrapperInitializer;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyMode;
import vahy.api.policy.PolicySupplier;
import vahy.impl.episode.EpisodeSetupImpl;
import vahy.impl.episode.EpisodeSimulatorImpl;
import vahy.utils.EnumUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.SplittableRandom;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class GameSamplerImpl<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends State<TAction, TObservation, TState>>
    implements GameSampler<TAction, TObservation, TState> {

    private static final Logger logger = LoggerFactory.getLogger(GameSamplerImpl.class.getName());

    private final InitialStateSupplier<TAction, TObservation, TState> initialStateSupplier;
    private final StateWrapperInitializer<TAction, TObservation, TState> stateStateWrapperInitializer;
    private final EpisodeResultsFactory<TAction, TObservation, TState> resultsFactory;
    private final int processingUnitCount;

    private final int totalPolicyCount;
    private final List<PolicyCategoryInfo> expectedPolicyCategoryInfoList;
    private final List<PolicyCategory<TAction, TObservation, TState>> policyCategoryList;
    private final PolicyShuffleStrategy policyShuffleStrategy;

    private final Random random;

    public GameSamplerImpl(InitialStateSupplier<TAction, TObservation, TState> initialStateSupplier,
                           StateWrapperInitializer<TAction, TObservation, TState> stateStateWrapperInitializer,
                           EpisodeResultsFactory<TAction, TObservation, TState> resultsFactory,
                           List<PolicyCategory<TAction, TObservation, TState>> policyCategoryList,
                           PolicyShuffleStrategy policyShuffleStrategy,
                           int processingUnitCount,
                           List<PolicyCategoryInfo> expectedPolicyCategoryInfoList,
                           SplittableRandom random)
    {
        this.stateStateWrapperInitializer = stateStateWrapperInitializer;
        this.random = new Random(random.nextInt());
        this.initialStateSupplier = initialStateSupplier;
        this.resultsFactory = resultsFactory;
        this.processingUnitCount = processingUnitCount;
        this.policyCategoryList = policyCategoryList;
        this.totalPolicyCount = policyCategoryList.stream().mapToInt(x -> x.getPolicySupplierList().size()).sum();
        this.policyShuffleStrategy = policyShuffleStrategy;
        this.expectedPolicyCategoryInfoList = expectedPolicyCategoryInfoList;
        checkPolicyCount(expectedPolicyCategoryInfoList, policyCategoryList);
    }

    private void checkPolicyCount(List<PolicyCategoryInfo> requestedCategoryList, List<PolicyCategory<TAction, TObservation, TState>> providedPolicyCategories) {
        for (int i = 0; i < requestedCategoryList.size(); i++) {
            var requestedCategory = requestedCategoryList.get(i);
            var providedCategory = providedPolicyCategories.get(i);
            if(requestedCategory.getCategoryId() != providedCategory.getCategoryId()) {
                throw new IllegalStateException("Different categoryIDs in expected " + requestedCategory.getCategoryId() + "] and provided [" + providedCategory.getCategoryId() + "] categoryList");
            }
            if(requestedCategory.getPolicyInCategoryCount() != providedCategory.getPolicySupplierList().size()) {
                throw new IllegalStateException("Different count of expected and provided policies. Policy requested count: [" + requestedCategoryList.get(i).getPolicyInCategoryCount() +
                    "]. Policy provided count: [" + providedPolicyCategories.get(i).getPolicySupplierList().size() +
                    "] for category: [" + i + "]");
            }
        }
    }

    private PolicyIdTranslationMap createPolicyTranslationMap(List<PolicyCategoryInfo> requestedCategoryList, List<PolicyCategory<TAction, TObservation, TState>> providedPolicyCategories) {
        var map = new PolicyIdTranslationMap(this.totalPolicyCount);

        switch (policyShuffleStrategy) {
            case NO_SHUFFLE: {
                var inGameId = 0;
                for (var category : providedPolicyCategories) {
                    var categoryPolicyList = category.getPolicySupplierList();
                    for (var entry : categoryPolicyList) {
                        map.put(entry.getPolicyId(), inGameId);
                        inGameId++;
                    }
                }
            }
            break;
            case CATEGORY_SHUFFLE: {
                var inGameId = 0;
                for (int i = 0; i < providedPolicyCategories.size(); i++) {
                    var isShufflePossible = requestedCategoryList.get(i).isShufflePossible();
                    var category = providedPolicyCategories.get(i);
                    var categoryPolicyList = category.getPolicySupplierList();
                    var policyIndexList = categoryPolicyList.stream().map(PolicySupplier::getPolicyId).collect(Collectors.toList());
                    if(isShufflePossible) {
                        Collections.shuffle(policyIndexList, random);
                    }
                    for (var entry : policyIndexList) {
                        map.put(entry, inGameId);
                        inGameId++;
                    }
                }
            }
            break;
            default: throw EnumUtils.createExceptionForNotExpectedEnumValue(policyShuffleStrategy);
        }
        return map;
    }

    private List<RegisteredPolicy<TAction, TObservation, TState>> initializeAndRegisterPolicies(TState initialState, PolicyMode policyMode, PolicyIdTranslationMap policyIdTranslationMap) {
        var registeredPolicyList = new ArrayList<RegisteredPolicy<TAction, TObservation, TState>>(this.totalPolicyCount);
        for (var category : this.policyCategoryList) {
            for (var entry : category.getPolicySupplierList()) {
                var inGameEntityId = policyIdTranslationMap.getInGameEntityId(entry.getPolicyId());
                registeredPolicyList.add(new RegisteredPolicy<>(entry.initializePolicy(stateStateWrapperInitializer.createInitialStateWrapper(inGameEntityId, initialState), policyMode), inGameEntityId));
            }
        }
        return registeredPolicyList;
    }

    @Override
    public List<EpisodeResults<TAction, TObservation, TState>> sampleEpisodes(int episodeBatchSize, int stepCountLimit, PolicyMode policyMode) {
        ExecutorService executorService = Executors.newFixedThreadPool(processingUnitCount);
        logger.info("Initialized [{}] executors for sampling", processingUnitCount);
        logger.info("Sampling [{}] episodes started", episodeBatchSize);
        var episodesToSample = new ArrayList<Callable<EpisodeResults<TAction, TObservation, TState>>>(episodeBatchSize);
        for (int i = 0; i < episodeBatchSize; i++) {
            TState initialGameState = initialStateSupplier.createInitialState(policyMode);
            var policyIdTranslationMap = createPolicyTranslationMap(expectedPolicyCategoryInfoList, policyCategoryList);
            var registeredPolicies = initializeAndRegisterPolicies(initialGameState, policyMode, policyIdTranslationMap);
            var paperEpisode = new EpisodeSetupImpl<>(initialGameState, policyIdTranslationMap, registeredPolicies, stepCountLimit);
            var episodeSimulator = new EpisodeSimulatorImpl<>(resultsFactory);
            episodesToSample.add(() -> episodeSimulator.calculateEpisode(paperEpisode));
        }
        try {
            var results = executorService.invokeAll(episodesToSample);
            var paperEpisodeHistoryList = results.stream().map(x -> {
                try {
                    return x.get();
                } catch (Throwable e) {
                    executorService.shutdown();
                    throw new IllegalStateException("Parallel episodes were interrupted.", e);
                }
            }).collect(Collectors.toList());

            executorService.shutdown();
            return paperEpisodeHistoryList;
        } catch (Throwable e) {
//            e.printStackTrace();
            throw new IllegalStateException("Parallel episodes were interrupted.", e);
        }
    }

}
