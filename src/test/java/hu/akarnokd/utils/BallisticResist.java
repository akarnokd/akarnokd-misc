package hu.akarnokd.utils;

public class BallisticResist {

    public static void main(String[] args) {

        double baseDamage = 103;
        double armorRemaining = (1 - 0.36) * (1 - 0.5);
        double speedCoeff = 1.0;
        
        System.out.println(baseDamage + "-" + (25 * armorRemaining) + ": " + damageFormula(baseDamage, 25 * armorRemaining) * speedCoeff);
        System.out.println(baseDamage + "-" + (50* armorRemaining) + ": " + damageFormula(baseDamage, 50 * armorRemaining) * speedCoeff);
        System.out.println(baseDamage + "-" + (75 * armorRemaining) + ": " + damageFormula(baseDamage, 75 * armorRemaining) * speedCoeff);
        System.out.println(baseDamage + "-" + (100 * armorRemaining) + ": " + damageFormula(baseDamage, 100 * armorRemaining) * speedCoeff);
        System.out.println(baseDamage + "-" + (150 * armorRemaining) + ": " + damageFormula(baseDamage, 150 * armorRemaining) * speedCoeff);
        System.out.println(baseDamage + "-" + (200 * armorRemaining) + ": " + damageFormula(baseDamage, 200 * armorRemaining) * speedCoeff);
        System.out.println(baseDamage + "-" + (300 * armorRemaining) + ": " + damageFormula(baseDamage, 300 * armorRemaining) * speedCoeff);

        System.out.println("SBQ: " + 48000 / (damageFormula(baseDamage, 300 * armorRemaining) * speedCoeff));

        System.out.println("Protectron   : " + (damageFormula(baseDamage, 125 * (1 - 0.36))));
        System.out.println("Protectron AA: " + (damageFormula(baseDamage, 125 * (1 - 0.36) * (1 - 0.5))));
        System.out.println("Protectron TR: " + (damageFormula(baseDamage * 1.3, 125 * (1 - 0.36))));

        System.out.println("Protectron TR: " + (damageFormula(baseDamage, 125)));
        System.out.println("Protectron AA: " + (damageFormula(baseDamage, 125 * (1 - 0.5))));
        System.out.println("Protectron TR: " + (damageFormula(baseDamage * 1.3, 125)));

        System.out.println("----------");
        
        System.out.println("Damage: " + (damageFormula(125, 120 * (1 - 0.36) * (1 - 0.4))));

        System.out.println("----------");

        
        /*
        System.out.println("42-25: " + damageFormula(42, 25));
        System.out.println("42-50: " + damageFormula(42, 50));
        System.out.println("42-75: " + damageFormula(42, 75));
        System.out.println("42-100: " + damageFormula(42, 100));
        System.out.println("42-150: " + damageFormula(42, 150));
        System.out.println("42-200: " + damageFormula(42, 200));
        System.out.println("42-300: " + damageFormula(42, 300));

        System.out.println("84-25: " + damageFormula(84, 25));
        System.out.println("84-50: " + damageFormula(84, 50));
        System.out.println("84-75: " + damageFormula(84, 75));
        System.out.println("84-100: " + damageFormula(84, 100));
        System.out.println("84-150: " + damageFormula(84, 150));
        System.out.println("84-200: " + damageFormula(84, 200));
        System.out.println("84-300: " + damageFormula(84, 300));

        System.out.println("---------");

        System.out.println("125-185: " + damageFormula(125, 185));
        System.out.println("100-185/2: " + damageFormula(100, 185/2));

        System.out.println("100-185: " + damageFormula(100, 185));
        System.out.println("25-185: " + damageFormula(25, 185));

        System.out.println("77-185*36%: " + damageFormula(77, 185 * 36 / 100));

        System.out.println("77-185*36%*40%: " + damageFormula(77, 185 * 36 / 100 * 40 / 100));

        System.out.println("140-185*45%: " + damageFormula(140, 185 * 45 / 100));

        System.out.println("---------");

        System.out.println("Plain Lever: " + damageFormula(118, 120));
        System.out.println("TS Lever:    " + damageFormula(140, 120));
        System.out.println("TS Lever':   " + (damageFormula(118, 120) + damageFormula(118 * 0.25, 120)));

        System.out.println("Plain Lever * 36%: " + damageFormula(118, 80));
        System.out.println("TS Lever * 36%  :  " + damageFormula(140, 80));
        System.out.println("TS Lever' * 36% :  " + (damageFormula(118, 80) + damageFormula(118 * 0.25, 80)));

        System.out.println("---------");
        
        double h = 700;

        System.out.println("Plain Lever: " + h / damageFormula(118, 125));
        System.out.println("TS Lever:    " + h / damageFormula(140, 125));
        System.out.println("TS Lever':   " + h / (damageFormula(118, 125) + damageFormula(118 * 0.25, 125)));
        System.out.println("TS Lever\":    " + h / damageFormula(140 * 1.1, 125));

        System.out.println("Plain Lever * 36%: " + h / damageFormula(118, 80));
        System.out.println("TS Lever * 36%  :  " + h / damageFormula(140, 80));
        System.out.println("TS Lever' * 36% :  " + h / (damageFormula(118, 80) + damageFormula(118 * 0.25, 80)));
        System.out.println("TS Lever\" * 36%  :  " + h / damageFormula(140 * 1.1, 80));

        
        System.out.println("---------");

        double baseDr = 300;
        double rof = 5;
        for (int j : new int[] { 77, 140 }) {
            for (int i = 1; i < 25; i++) {
                //System.out.println(j + "-" + (i * 50) + ": " + damageFormula(j, i * 50));
                double dmg = damageFormula(j, baseDr * 55 / 100);
                double ammo = i * 32000 * 1.5 / dmg;
                double timeMinutes = Math.ceil(ammo / 5 / 60 * 100) / 100.0;
                System.out.println(j + "-" + (baseDr * 55 / 100) + " [" + i + "] (%45 AA): " + dmg + " - " + ammo + " ammo" + ", " + timeMinutes + " minutes");
            }
        }
        */
    }
    
    static double damageFormula(double damage, double dr) {
        return Math.min(0.99, 0.5 * Math.pow((damage / dr), 0.366)) * damage;
    }
    
}
