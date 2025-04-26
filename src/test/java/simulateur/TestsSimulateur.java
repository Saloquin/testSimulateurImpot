package simulateur;

import com.kerware.simulateur.AdaptateurSimulateur;
import com.kerware.simulateur.ICalculateurImpot;
import com.kerware.simulateur.SituationFamiliale;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestsSimulateur {

    private static ICalculateurImpot simulateur;

    @BeforeAll
    public static void setUp() {
        simulateur = new AdaptateurSimulateur();
    }

    // EXG_IMPOT_03
    public static Stream<Arguments> donneesPartsFoyerFiscal() {
        return Stream.of(
                Arguments.of(24000, "CELIBATAIRE", 0, 0, false, 1),
                Arguments.of(24000, "CELIBATAIRE", 1, 0, false, 1.5),
                Arguments.of(50000, "PACSE", 0, 0, false, 2),
                Arguments.of(24000, "CELIBATAIRE", 2, 0, false, 2),
                Arguments.of(50000, "MARIE", 0, 0, false, 2),
                Arguments.of(50000, "DIVORCE", 0, 0, false, 1),
                Arguments.of(50000, "VEUF", 0, 0, false, 1),
                Arguments.of(50000, "CELIBATAIRE", 3, 0, false, 3),
                Arguments.of(50000, "CELIBATAIRE", 3, 1, false, 3.5),
                Arguments.of(50000, "VEUF", 1, 0, false, 2.5),
                Arguments.of(50000, "DIVORCE", 2, 1, true, 3)
        );
    }
    @ParameterizedTest
    @MethodSource("donneesPartsFoyerFiscal")
    public void testNombreDeParts(int revenuNet, String situationFamiliale, int nbEnfantsACharge, int nbEnfantsSituationHandicap, boolean parentIsole, double nbPartsAttendu) {
        simulateur.setRevenusNet(revenuNet);
        simulateur.setSituationFamiliale(SituationFamiliale.valueOf(situationFamiliale));
        simulateur.setNbEnfantsACharge(nbEnfantsACharge);
        simulateur.setNbEnfantsSituationHandicap(nbEnfantsSituationHandicap);
        simulateur.setParentIsole(parentIsole);

        simulateur.calculImpotSurRevenuNet();

        assertEquals(nbPartsAttendu, simulateur.getNbPartsFoyerFiscal());
        assertEquals(revenuNet, simulateur.getRevenuNet());
        assertEquals(revenuNet - simulateur.getAbattement(), simulateur.getRevenuReference());
    }

    // EXG_IMPOT_02
    public static Stream<Arguments> donneesAbattement() {
        return Stream.of(
                Arguments.of(50000, "CELIBATAIRE", 0, 0, false, 5000), // Abattement within limits
                Arguments.of(150000, "MARIE", 2, 0, false, 14171), // Abattement capped at max limit
                Arguments.of(3000, "DIVORCE", 1, 0, true, 495) // Abattement at min limit
        );
    }
    @ParameterizedTest
    @MethodSource("donneesAbattement")
    public void testAbattement(int revenuNet, String situationFamiliale, int nbEnfantsACharge, int nbEnfantsSituationHandicap, boolean parentIsole, double abattementAttendu) {
        simulateur.setRevenusNet(revenuNet);
        simulateur.setSituationFamiliale(SituationFamiliale.valueOf(situationFamiliale));
        simulateur.setNbEnfantsACharge(nbEnfantsACharge);
        simulateur.setNbEnfantsSituationHandicap(nbEnfantsSituationHandicap);
        simulateur.setParentIsole(parentIsole);

        simulateur.calculImpotSurRevenuNet();

        assertEquals(abattementAttendu, simulateur.getAbattement());
        assertEquals(revenuNet, simulateur.getRevenuNet());
    }

    // EXG_IMPOT_04
    public static Stream<Arguments> donneesImpotBrut() {
        return Stream.of(
                Arguments.of(50000, "CELIBATAIRE", 0, 0, false, 6786), // Example values
                Arguments.of(150000, "MARIE", 2, 0, false, 23803), // Example values
                Arguments.of(75000, "DIVORCE", 1, 0, true, 10018), // Example values
                Arguments.of(100000, "VEUF", 0, 0, false, 21129), // Example values
                Arguments.of(60000, "PACSE", 0, 0, false, 3455) // Example values
        );
    }

    @ParameterizedTest
    @MethodSource("donneesImpotBrut")
    public void testImpotBrut(int revenuNet, String situationFamiliale, int nbEnfantsACharge, int nbEnfantsSituationHandicap, boolean parentIsole, double impotBrutAttendu) {
        simulateur.setRevenusNet(revenuNet);
        simulateur.setSituationFamiliale(SituationFamiliale.valueOf(situationFamiliale));
        simulateur.setNbEnfantsACharge(nbEnfantsACharge);
        simulateur.setNbEnfantsSituationHandicap(nbEnfantsSituationHandicap);
        simulateur.setParentIsole(parentIsole);

        simulateur.calculImpotSurRevenuNet();

        assertEquals(impotBrutAttendu, simulateur.getImpotAvantDecote());
    }

    // EXG_IMPOT_05

    // EXG_IMPOT_06
    public static Stream<Arguments> donneesDecote() {
        return Stream.of(
                Arguments.of(50000, "CELIBATAIRE", 0, 0, false, 6786, 0), // Decote for single declarant
                Arguments.of(150000, "MARIE", 2, 0, false, 23803, 0), // Decote for couple
                Arguments.of(75000, "DIVORCE", 1, 0, true, 10018, 0), // Decote for single declarant with child
                Arguments.of(100000, "VEUF", 0, 0, false, 21129, 0), // Decote for single declarant
                Arguments.of(60000, "PACSE", 0, 0, false, 3455, 0), // Decote for couple
                Arguments.of(18000, "CELIBATAIRE", 0, 0, false, 540, 540), // Decote for single declarant below threshold
                Arguments.of(30000, "MARIE", 0, 0, false, 485, 485) // Decote for couple below threshold
        );
    }

    @ParameterizedTest
    @MethodSource("donneesDecote")
    public void testDecote(int revenuNet, String situationFamiliale, int nbEnfantsACharge, int nbEnfantsSituationHandicap, boolean parentIsole, double impotBrutAttendu, double decoteAttendu) {
        simulateur.setRevenusNet(revenuNet);
        simulateur.setSituationFamiliale(SituationFamiliale.valueOf(situationFamiliale));
        simulateur.setNbEnfantsACharge(nbEnfantsACharge);
        simulateur.setNbEnfantsSituationHandicap(nbEnfantsSituationHandicap);
        simulateur.setParentIsole(parentIsole);

        simulateur.calculImpotSurRevenuNet();

        assertEquals(impotBrutAttendu, simulateur.getImpotAvantDecote());
        assertEquals(revenuNet, simulateur.getRevenuNet());
        assertEquals(decoteAttendu, simulateur.getDecote());
    }

    // Tests de robustesse


    public static Stream<Arguments> donneesRobustesse() {
        return Stream.of(
                Arguments.of(-50000, "CELIBATAIRE", 0, 0, false, "Le revenu net ne peut pas être négatif"),
                Arguments.of(50000, "CELIBATAIRE", -1, 0, false, "Le nombre d'enfants ne peut pas être négatif"),
                Arguments.of(50000, "CELIBATAIRE", 0, -1, false, "Le nombre d'enfants handicapés ne peut pas être négatif"),
                Arguments.of(50000, null, 0, 0, false, "La situation familiale ne peut pas être nulle"),
                Arguments.of(50000, "CELIBATAIRE", 2, 3, false, "Le nombre d'enfants handicapés ne peut pas être supérieur au nombre d'enfants"),
                Arguments.of(50000, "CELIBATAIRE", 8, 0, false, "Le nombre d'enfants ne peut pas être supérieur à 7"),
                Arguments.of(50000, "MARIE", 0, 0, true, "Un parent isolé ne peut pas être marié ou pacsé")
        );
    }

    @ParameterizedTest
    @MethodSource("donneesRobustesse")
    public void testRobustesse(int revenuNet, String situationFamiliale, int nbEnfantsACharge, int nbEnfantsSituationHandicap, boolean parentIsole, String messageAttendu) {
        simulateur.setRevenusNet(revenuNet);
        if (situationFamiliale != null) {
            simulateur.setSituationFamiliale(SituationFamiliale.valueOf(situationFamiliale));
        }else{
            simulateur.setSituationFamiliale(null);
        }
        simulateur.setNbEnfantsACharge(nbEnfantsACharge);
        simulateur.setNbEnfantsSituationHandicap(nbEnfantsSituationHandicap);
        simulateur.setParentIsole(parentIsole);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            simulateur.calculImpotSurRevenuNet();
        });

        assertEquals(messageAttendu, exception.getMessage());
    }



    // TODO : FAIRE UNE SERIE DE TESTS SUPPLEMENTAIRES POUR COUVRIR TOUTES LES EXIGENCES
    // AVEC D'AUTRES IDEES DE TESTS
    // AVEC @ParameterizedTest et @CsvFileSource

}
