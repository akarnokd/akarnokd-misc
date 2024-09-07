package hu.akarnokd.theplanetcrafter;

public class InventoryPrintLn {
    public static void main(String[] args) {
        for (int i = 3036; i < 3200; i++) {
            System.out.printf("{\"id\":%s,\"woIds\":\"\",\"size\": 80,\"demandGrps\":\"\",\"supplyGrps\":\"\",\"priority\":0}|%n", i);
        }
    }
}
