package tm;

import java.util.HashMap;
import java.util.Map;

public class MapData {

    public static final Map<String, MapData> mapsById = new HashMap<>();
    public static final Map<String, MapData> mapsByName = new HashMap<>();

    private final String name;
    private final String[] data;

    public static void init() {
        final String[] baseMapData = {
                "U,S,G,B,Y,R,U,K,R,G,B,R,K",
                "Y,I,I,U,K,I,I,Y,K,I,I,Y",
                "I,I,K,I,S,I,G,I,G,I,S,I,I",
                "G,B,Y,I,I,R,B,I,R,I,R,U",
                "K,U,R,B,K,U,S,Y,I,I,G,K,B",
                "S,G,I,I,Y,G,I,I,I,U,S,U",
                "I,I,I,S,I,R,I,G,I,Y,K,B,Y",
                "Y,B,U,I,I,I,B,K,I,S,U,S",
                "R,K,S,B,R,G,Y,U,S,I,B,G,R",
        };

        final String[] arrowMapData = {
                "G,B,Y,U,G,Y,R,B,S,G,S,G,K",
                "R,K,B,I,I,I,I,I,I,Y,R,B",
                "S,U,I,I,S,R,Y,U,R,I,I,K,Y",
                "B,R,I,G,B,K,I,S,B,I,G,U",
                "G,S,G,I,U,I,I,I,G,I,I,I,I",
                "U,I,I,Y,K,R,I,K,Y,R,I,K",
                "R,K,Y,I,I,S,Y,B,U,I,I,U,B",
                "B,S,U,I,I,I,I,I,I,B,G,S",
                "K,R,G,S,K,S,G,K,R,Y,U,R,Y",
        };

        new MapData("be8f6ebf549404d015547152d5f2a1906ae8dd90", "Revised Base", null);
        new MapData("91645cdb135773c2a7a50e5ca9cb18af54c664c4", "Base", baseMapData);
    }

    public MapData(String id, String name, String[] data) {
        mapsById.put(id, this);
        mapsByName.put(name, this);
        this.name = name;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public String[] getData() {
        return data;
    }


}
