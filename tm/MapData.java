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

        final String[] fireAndIce = {
                "U,I,U,K,Y,I,S,G,R,B,Y,B,N",
                "R,Y,I,B,S,R,I,I,I,Y,U,K,S",
                "G,K,I,I,I,U,G,Y,I,I,I,I,N",
                "Y,S,G,Y,K,I,B,R,U,I,G,B,G",
                "I,I,U,I,I,R,K,G,S,I,U,K,N",
                "G,R,I,I,G,I,I,I,U,B,I,S,R",
                "S,I,Y,S,B,R,G,I,R,S,I,K,N",
                "K,B,I,K,U,S,B,I,Y,K,I,R,B",
                "S,G,I,R,Y,K,Y,I,B,U,I,U,N",
        };

        final String[] revisedBase = {
                "U,S,G,B,U,R,U,K,R,B,G,R,K",
                "Y,I,I,Y,K,I,I,Y,G,I,I,Y,N",
                "I,I,K,I,S,I,G,I,K,I,R,I,I",
                "G,B,Y,I,I,R,B,I,R,I,S,U,N",
                "K,U,R,B,Y,U,G,Y,I,I,G,K,R",
                "S,G,I,I,K,S,I,I,I,U,S,Y,N",
                "I,I,I,S,I,R,I,G,I,Y,K,B,U",
                "Y,B,U,I,I,I,B,K,I,S,U,R,N",
                "B,K,S,B,R,G,Y,U,S,I,B,G,S",
        };

        final String[] loonLakes = {
                "S,B,R,U,Y,B,Y,R,I,I,G,B,N",
                "Y,K,G,I,I,K,U,I,G,S,I,U,K",
                "U,I,I,G,R,S,I,K,B,R,I,Y,N",
                "B,R,S,I,Y,U,G,I,I,Y,I,K,R",
                "G,Y,I,K,B,I,I,R,I,S,G,U,N",
                "S,I,U,S,I,Y,I,S,I,U,K,B,R",
                "R,I,I,I,R,G,U,K,Y,I,I,S,N",
                "Y,B,K,I,B,S,B,I,I,S,G,I,B",
                "K,U,I,G,I,I,I,G,R,U,Y,K,N",
        };

        final String[] fjords = {
                "G,K,I,U,Y,S,K,S,Y,R,K,B,Y",
                "B,U,I,B,G,R,I,I,I,I,I,U,N",
                "S,G,R,I,I,U,I,K,S,U,Y,I,S",
                "I,I,I,S,I,I,G,R,B,G,R,I,N",
                "R,S,Y,I,B,R,I,U,Y,S,U,I,K",
                "K,U,I,G,Y,G,I,S,B,G,I,S,N",
                "Y,B,I,K,S,K,B,I,U,K,I,G,R",
                "G,I,U,R,U,Y,R,I,I,I,R,B,N",
                "K,I,I,G,B,S,B,I,G,Y,K,U,Y",
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

        new MapData("91645cdb135773c2a7a50e5ca9cb18af54c664c4", "Base", baseMapData);
        new MapData("95a66999127893f5925a5f591d54f8bcb9a670e6", "F&I", fireAndIce);
        new MapData("2afadc63f4d81e850b7c16fb21a1dcd29658c392", "Fjords", fjords);
        new MapData("fdb13a13cd48b7a3c3525f27e4628ff6905aa5b1", "Loon Lakes", loonLakes);
        new MapData("be8f6ebf549404d015547152d5f2a1906ae8dd90", "Revised Base", revisedBase);
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
