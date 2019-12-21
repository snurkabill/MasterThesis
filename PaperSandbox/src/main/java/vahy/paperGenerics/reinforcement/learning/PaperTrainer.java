package vahy.paperGenerics.reinforcement.learning;

//
//public class PaperTrainer<
//    TAction extends Enum<TAction> & Action<TAction>,
//    TOpponentObservation extends Observation,
//    TState extends PaperState<TAction, DoubleVector, TOpponentObservation, TState>,
//    TPolicyRecord extends PolicyRecordBase>
//    extends Trainer<TAction, DoubleVector, TOpponentObservation, TState, TPolicyRecord> {
//
//    private static final Logger logger = LoggerFactory.getLogger(PaperTrainer.class.getName());
//
//    private final double discountFactor;
//
//    public PaperTrainer(GameSampler<TAction, DoubleVector, TOpponentObservation, TState, TPolicyRecord> gameSampler,
//                        TrainablePredictor trainablePredictor,
//                        double discountFactor,
//                        DataAggregator dataAggregator, ) {
//        super(trainablePredictor, gameSampler, dataAggregator, episodeDataMaker);
//        this.discountFactor = discountFactor;
//    }
////
////    @Override
////    protected List<ImmutableTuple<DoubleVector, MutableDoubleArray>> createEpisodeDataSamples(
////        EpisodeResults<TAction, DoubleVector, TOpponentObservation, TState, TPolicyRecord> paperEpisode) {
////        var episodeHistory = paperEpisode.getEpisodeHistory();
////        var aggregatedRisk = episodeHistory.get(episodeHistory.size() - 1).getToState().isRiskHit() ? 1.0 : 0.0;
////        var aggregatedTotalPayoff = 0.0;
////        var iterator = episodeHistory.listIterator(paperEpisode.getTotalStepCount());
////        var mutableDataSampleList = new ArrayList<ImmutableTuple<DoubleVector, MutableDoubleArray>>();
////        while(iterator.hasPrevious()) {
////            var previous = iterator.previous();
////            aggregatedTotalPayoff = DoubleScalarRewardAggregator.aggregateDiscount(previous.getReward(), aggregatedTotalPayoff, discountFactor);
////            if(previous.isPlayerMove()) {
////                var policyArray = previous.getPolicyStepRecord().getPolicyProbabilities();
////                var doubleArray = new double[policyArray.length + PaperModel.POLICY_START_INDEX];
////                doubleArray[PaperModel.Q_VALUE_INDEX] = aggregatedTotalPayoff;
////                doubleArray[PaperModel.RISK_VALUE_INDEX] = aggregatedRisk;
////                System.arraycopy(policyArray, 0, doubleArray, PaperModel.POLICY_START_INDEX, policyArray.length);
////                mutableDataSampleList.add(new ImmutableTuple<>(
////                    previous.getFromState().getPlayerObservation(),
////                    new MutableDoubleArray(doubleArray, false)));
////            }
////        }
////        Collections.reverse(mutableDataSampleList);
////        return mutableDataSampleList;
////    }
//
////    protected double[] createOutputVector(MutableDataSample dataSample) {
////        double[] probabilities = dataSample.getProbabilities();
////        double[] outputVector = new double[probabilities.length + PaperModel.POLICY_START_INDEX];
////        outputVector[PaperModel.Q_VALUE_INDEX] = dataSample.getReward();
////        outputVector[PaperModel.RISK_VALUE_INDEX] = dataSample.getRisk();
////        for (int i = 0; i < probabilities.length; i++) {
////            outputVector[i + PaperModel.POLICY_START_INDEX] = probabilities[i];
////        }
////        return outputVector;
////    }
//
//}
