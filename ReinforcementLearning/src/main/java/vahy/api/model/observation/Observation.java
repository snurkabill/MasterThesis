package vahy.api.model.observation;

import java.util.List;

public interface Observation<T extends Observation<T>> {

    T groupArrayOfObservations(T[] observationArray);

    T groupListOfObservations(List<T> observationArray);

}
