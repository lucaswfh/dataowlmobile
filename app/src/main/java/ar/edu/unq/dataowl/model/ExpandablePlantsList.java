package ar.edu.unq.dataowl.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExpandablePlantsList {

    public static HashMap<String, List<String>> getData() {
        HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();

        List<String> plantType = new ArrayList<String>();
        plantType.add("Pasto cuaresma (Digitaria sanguinalis)");
        plantType.add("Capín (Echinochloa grusgalli)");
        plantType.add("Pata de ganso (Eleusine indica)");
        plantType.add("Rama negra (Conyza bonariensis)");
        plantType.add("Yuyo colorado (Amaranthus hybridus)");
        plantType.add("Malva cimarrona (Anoda cristata)");
        plantType.add("Quinoa blanca (Chenopodium album)");

        expandableListDetail.put("Plants Types", plantType);
        return expandableListDetail;
    }

}
