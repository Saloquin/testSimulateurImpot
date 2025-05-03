package simulateur;

import com.kerware.simulateurreusine.AdaptateurSimulateurReusine;
import com.kerware.simulateurreusine.ICalculateurImpot;
import com.kerware.simulateurreusine.SituationFamiliale;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestsSimulateur {

    private static ICalculateurImpot simulateur;

    @BeforeAll
    public static void setUp() {
        simulateur = new AdaptateurSimulateurReusine();
    }

    public static Stream<Arguments> donneesPartsFoyerFiscal() {
        return Stream.of(
                Arguments.of(24000, "CELIBATAIRE", 0, 0, false, 1),
                Arguments.of(24000, "CELIBATAIRE", 1, 0, false, 1.5),
                Arguments.of(24000, "CELIBATAIRE", 2, 0, false, 2),
                Arguments.of(24000, "CELIBATAIRE", 3, 0, false, 3),
                Arguments.of(24000, "MARIE", 0, 0, false, 2),
                Arguments.of(24000, "PACSE", 0, 0, false, 2),
                Arguments.of(24000, "MARIE", 3, 1, false, 4.5),
                Arguments.of(24000, "DIVORCE", 2, 0, true, 2.5),
                Arguments.of(24000, "VEUF", 3, 0, true, 4.5)
                );

    }

    // COUVERTURE EXIGENCE : EXG_IMPOT_03
    @DisplayName("Tests du calcul des parts pour différents foyers fiscaux")
    @ParameterizedTest
    @MethodSource( "donneesPartsFoyerFiscal" )
    public void testNombreDeParts( int revenuNetDeclarant1, String situationFamiliale, int nbEnfantsACharge,
                                   int nbEnfantsSituationHandicap, boolean parentIsole, double nbPartsAttendu) {

        // Arrange
        simulateur.setRevenusNetDeclarant1( revenuNetDeclarant1 );
        simulateur.setRevenusNetDeclarant2( 0);
        simulateur.setSituationFamiliale( SituationFamiliale.valueOf(situationFamiliale) );
        simulateur.setNbEnfantsACharge( nbEnfantsACharge );
        simulateur.setNbEnfantsSituationHandicap( nbEnfantsSituationHandicap );
        simulateur.setParentIsole( parentIsole );

        // Act
        simulateur.calculImpotSurRevenuNet();

        // Assert
        assertEquals(   nbPartsAttendu, simulateur.getNbPartsFoyerFiscal());

    }


    public static Stream<Arguments> donneesAbattementFoyerFiscal() {
        return Stream.of(
                Arguments.of(4900, "CELIBATAIRE", 0, 0, false, 495), // < 495 => 495
                Arguments.of(12000, "CELIBATAIRE", 0, 0, false, 1200), // 10 %
                Arguments.of(200000, "CELIBATAIRE", 0, 0, false, 14171) // > 14171 => 14171
        );

    }

    // COUVERTURE EXIGENCE : EXG_IMPOT_03
    @DisplayName("Tests des abattements pour les foyers fiscaux")
    @ParameterizedTest
    @MethodSource( "donneesAbattementFoyerFiscal" )
    public void testAbattement( int revenuNetDeclarant1, String situationFamiliale, int nbEnfantsACharge,
                                   int nbEnfantsSituationHandicap, boolean parentIsole, int abattementAttendu) {

        // Arrange
        simulateur.setRevenusNetDeclarant1( revenuNetDeclarant1 );
        simulateur.setRevenusNetDeclarant2( 0);
        simulateur.setSituationFamiliale( SituationFamiliale.valueOf(situationFamiliale) );
        simulateur.setNbEnfantsACharge( nbEnfantsACharge );
        simulateur.setNbEnfantsSituationHandicap( nbEnfantsSituationHandicap );
        simulateur.setParentIsole( parentIsole );

        // Act
        simulateur.calculImpotSurRevenuNet();

        // Assert
        assertEquals(   abattementAttendu, simulateur.getAbattement());
    }


    public static Stream<Arguments> donneesRevenusFoyerFiscal() {
        return Stream.of(
                Arguments.of(12000, "CELIBATAIRE", 0, 0, false, 0), // 0%
                Arguments.of(20000, "CELIBATAIRE", 0, 0, false, 199), // 11%
                Arguments.of(35000, "CELIBATAIRE", 0, 0, false, 2736 ), // 30%
                Arguments.of(95000, "CELIBATAIRE", 0, 0, false, 19284), // 41%
                Arguments.of(200000, "CELIBATAIRE", 0, 0, false, 60768) // 45%
        );

    }

    // COUVERTURE EXIGENCE : EXG_IMPOT_04
    @DisplayName("Tests des différents taux marginaux d'imposition")
    @ParameterizedTest
    @MethodSource( "donneesRevenusFoyerFiscal" )
    public void testTrancheImposition( int revenuNet, String situationFamiliale, int nbEnfantsACharge,
                                int nbEnfantsSituationHandicap, boolean parentIsole, int impotAttendu) {

        // Arrange
        simulateur.setRevenusNetDeclarant1( revenuNet );
        simulateur.setRevenusNetDeclarant2( 0);
        simulateur.setSituationFamiliale( SituationFamiliale.valueOf(situationFamiliale) );
        simulateur.setNbEnfantsACharge( nbEnfantsACharge );
        simulateur.setNbEnfantsSituationHandicap( nbEnfantsSituationHandicap );
        simulateur.setParentIsole( parentIsole );

        // Act
        simulateur.calculImpotSurRevenuNet();

        // Assert
        assertEquals(   impotAttendu, simulateur.getImpotSurRevenuNet());
    }

    public static Stream<Arguments> donneesContributionExceptionnelle() {
        return Stream.of(
                Arguments.of(250000, "CELIBATAIRE", 0, 0, false, 0),
                Arguments.of(300000, "CELIBATAIRE", 0, 0, false, 1075),
                Arguments.of(600000, "CELIBATAIRE", 0, 0, false, 10933),
                Arguments.of(1200000, "CELIBATAIRE", 0, 0, false, 34933),
                Arguments.of(600000, "MARIE", 0, 0, false, 2560)
        );
    }

    // COUVERTURE EXIGENCE : EXG_IMPOT_07
    @DisplayName("Tests de la contribution exceptionnelle sur les hauts revenus")
    @ParameterizedTest
    @MethodSource("donneesContributionExceptionnelle")
    public void testContributionExceptionnelle(int revenuNet, String situationFamiliale, int nbEnfantsACharge,
                                               int nbEnfantsSituationHandicap, boolean parentIsole, int contributionAttendue) {

        // Arrange
        simulateur.setRevenusNetDeclarant1(revenuNet);
        simulateur.setRevenusNetDeclarant2(0);
        simulateur.setSituationFamiliale(SituationFamiliale.valueOf(situationFamiliale));
        simulateur.setNbEnfantsACharge(nbEnfantsACharge);
        simulateur.setNbEnfantsSituationHandicap(nbEnfantsSituationHandicap);
        simulateur.setParentIsole(parentIsole);

        // Act
        simulateur.calculImpotSurRevenuNet();

        // Assert
        assertEquals(contributionAttendue, simulateur.getContribExceptionnelle());
    }


    public static Stream<Arguments> donneesRobustesse() {
        return Stream.of(
                Arguments.of(-1, 0,"CELIBATAIRE", 0, 0, false), // 0%
                Arguments.of(20000,0, null , 0, 0, false), // 11%
                Arguments.of(35000,0, "CELIBATAIRE", -1, 0, false ), // 30%
                Arguments.of(95000,0, "CELIBATAIRE", 0, -1, false), // 41%
                Arguments.of(200000,0, "CELIBATAIRE", 3, 4, false, 60768),
                Arguments.of(200000,0, "MARIE", 3, 2, true),
                Arguments.of(200000,0, "PACSE", 3, 2, true),
                Arguments.of(200000,0, "MARIE", 8, 0, false),
                Arguments.of(200000,10000, "CELIBATAIRE", 8, 0, false),
                Arguments.of(200000,10000, "VEUF", 8, 0, false),
                Arguments.of(200000,10000, "DIVORCE", 8, 0, false)
        );
    }

    public static Stream<Arguments> donneesRevenusNetDeclatant1() {
        return Stream.of(
                Arguments.of(0, 0),
                Arguments.of(15000, 15000),
                Arguments.of(30000, 30000)
        );
    }
    @DisplayName("Tests du revenu net du premier déclarant")
    @ParameterizedTest
    @MethodSource("donneesRevenusNetDeclatant1")
    public void testGetRevenuNetDeclatant1(int revenuNetDecl1, int revenuAttendu) {
        // Arrange
        simulateur.setRevenusNetDeclarant1(revenuNetDecl1);

        // Act
        int result = simulateur.getRevenuNetDeclatant1();

        // Assert
        assertEquals(revenuAttendu, result);
    }

    public static Stream<Arguments> donneesRevenusNetDeclatant2() {
        return Stream.of(
                Arguments.of(0, 0),
                Arguments.of(15000, 15000),
                Arguments.of(30000, 30000)
        );
    }

    @DisplayName("Tests du revenu net du second déclarant")
    @ParameterizedTest
    @MethodSource("donneesRevenusNetDeclatant2")
    public void testGetRevenuNetDeclatant2(int revenuNetDecl2, int revenuAttendu) {
        // Arrange
        simulateur.setRevenusNetDeclarant2(revenuNetDecl2);

        // Act
        int result = simulateur.getRevenuNetDeclatant2();

        // Assert
        assertEquals(revenuAttendu, result);
    }

    public static Stream<Arguments> donneesRevenuFiscalReference() {
        return Stream.of(
                Arguments.of(20000, 10000, 27000), // Example with abattement applied
                Arguments.of(50000, 20000, 63000),
                Arguments.of(100000, 50000, 135000)
        );
    }

    @DisplayName("Tests du revenu fiscal de référence")
    @ParameterizedTest
    @MethodSource("donneesRevenuFiscalReference")
    public void testGetRevenuFiscalReference(int revenuNetDecl1, int revenuNetDecl2, int revenuFiscalAttendu) {
        // Arrange
        simulateur.setRevenusNetDeclarant1(revenuNetDecl1);
        simulateur.setRevenusNetDeclarant2(revenuNetDecl2);
        simulateur.setParentIsole(false);
        simulateur.setSituationFamiliale(SituationFamiliale.MARIE);
        simulateur.calculImpotSurRevenuNet();

        // Act
        int result = simulateur.getRevenuFiscalReference();

        // Assert
        assertEquals(revenuFiscalAttendu, result);
    }

    public static Stream<Arguments> donneesImpotAvantDecote() {
        return Stream.of(
                Arguments.of(20000, 0, "CELIBATAIRE", 738), // Single declarant, tax brackets applied
                Arguments.of(50000, 0, "CELIBATAIRE", 6786), // Single declarant, higher income
                Arguments.of(100000, 0, "CELIBATAIRE", 21129), // Single declarant, top brackets
                Arguments.of(50000, 50000, "MARIE", 13572), // Couple, combined income
                Arguments.of(100000, 100000, "MARIE", 42257) // Couple, higher combined income
        );
    }

    @DisplayName("Tests de l'impôt avant décote avec calcul des parts")
    @ParameterizedTest
    @MethodSource("donneesImpotAvantDecote")
    public void testGetImpotAvantDecote(int revenuNetDecl1, int revenuNetDecl2, String situationFamiliale, int impotAvantDecoteAttendu) {
        // Arrange
        simulateur.setRevenusNetDeclarant1(revenuNetDecl1);
        simulateur.setRevenusNetDeclarant2(revenuNetDecl2);
        simulateur.setSituationFamiliale(SituationFamiliale.valueOf(situationFamiliale));
        simulateur.calculImpotSurRevenuNet();

        // Act
        int result = simulateur.getImpotAvantDecote();

        // Assert
        assertEquals(impotAvantDecoteAttendu, result);
    }

    public static Stream<Arguments> donneesDecote() {
        return Stream.of(
                Arguments.of(1500, 0, "CELIBATAIRE", 1, 0),
                Arguments.of(2000, 0, "CELIBATAIRE", 1, 0),
                Arguments.of(1000, 0, "CELIBATAIRE", 1, 0),
                Arguments.of(3000, 0, "MARIE", 2, 0),
                Arguments.of(3500, 0, "MARIE", 2, 0)
        );
    }

    @DisplayName("Tests de la décote avec prise en compte du nombre de parts déclarants")
    @ParameterizedTest
    @MethodSource("donneesDecote")
    public void testCalculerDecote(int revenuNetDecl1, int revenuNetDecl2, String situationFamiliale,
                                   int nombrePartsDeclarants, int decoteAttendue) {
        // Arrange
        simulateur.setRevenusNetDeclarant1(revenuNetDecl1);
        simulateur.setRevenusNetDeclarant2(revenuNetDecl2);
        simulateur.setSituationFamiliale(SituationFamiliale.valueOf(situationFamiliale));
        simulateur.calculImpotSurRevenuNet();

        // Act
        int result = simulateur.getDecote();

        // Assert
        assertEquals(decoteAttendue, result);
    }



    // COUVERTURE EXIGENCE : Robustesse
    @DisplayName("Tests de robustesse avec des valeurs interdites")

    @ParameterizedTest( name ="Test avec revenuNetDeclarant1={0}, revenuDeclarant2={1}, situationFamiliale={2}, nbEnfantsACharge={3}, nbEnfantsSituationHandicap={4}, parentIsole={5}")
    @MethodSource( "donneesRobustesse" )
    public void testRobustesse( int revenuNetDeclarant1, int revenuNetDeclarant2 , String situationFamiliale, int nbEnfantsACharge,
                                       int nbEnfantsSituationHandicap, boolean parentIsole) {

        // Arrange
        simulateur.setRevenusNetDeclarant1( revenuNetDeclarant1 );
        simulateur.setRevenusNetDeclarant2( revenuNetDeclarant2 );
        if ( situationFamiliale == null )
                simulateur.setSituationFamiliale( null  );
        else
                simulateur.setSituationFamiliale( SituationFamiliale.valueOf( situationFamiliale ));
        simulateur.setNbEnfantsACharge( nbEnfantsACharge );
        simulateur.setNbEnfantsSituationHandicap( nbEnfantsSituationHandicap );
        simulateur.setParentIsole( parentIsole );

        // Act & Assert
        assertThrows( IllegalArgumentException.class,  () -> { simulateur.calculImpotSurRevenuNet();} );


    }

    // AVEC D'AUTRES IDEES DE TESTS
    // AVEC @ParameterizedTest et @CsvFileSource
    @DisplayName("Tests supplémentaires de cas variés de foyers fiscaux - ")
    @ParameterizedTest( name = " avec revenuNetDeclarant1={0}, revenuNetDeclarant2={1}, situationFamiliale={2}, nbEnfantsACharge={3}, nbEnfantsSituationHandicap={4}, parentIsole={5} - IMPOT NET ATTENDU = {6}")
    @CsvFileSource( resources={"/datasImposition.csv"} , numLinesToSkip = 1 )
    public void testCasImposition( int revenuNetDeclarant1, int revenuNetDeclarant2,  String situationFamiliale, int nbEnfantsACharge,
                                       int nbEnfantsSituationHandicap, boolean parentIsole, int impotAttendu) {

       // Arrange
        simulateur.setRevenusNetDeclarant1( revenuNetDeclarant1 );
        simulateur.setRevenusNetDeclarant2( revenuNetDeclarant2 );
        simulateur.setSituationFamiliale( SituationFamiliale.valueOf( situationFamiliale) );
        simulateur.setNbEnfantsACharge( nbEnfantsACharge );
        simulateur.setNbEnfantsSituationHandicap( nbEnfantsSituationHandicap );
        simulateur.setParentIsole( parentIsole );

        // Act
        simulateur.calculImpotSurRevenuNet();

        // Assert
        assertEquals(   Integer.valueOf(impotAttendu), simulateur.getImpotSurRevenuNet());
    }

}
