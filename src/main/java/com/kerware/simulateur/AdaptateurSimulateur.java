package com.kerware.simulateur;

public class AdaptateurSimulateur implements ICalculateurImpot {

    private Simulateur simulateur = new Simulateur();

    private int revenusNet;
    private SituationFamiliale situationFamiliale;
    private int nbEnfantsACharge;
    private int nbEnfantsSituationHandicap;
    private boolean parentIsole;

    @Override
    public void setRevenusNet(int rn) {
        this.revenusNet = rn;
    }

    @Override
    public void setSituationFamiliale(SituationFamiliale sf) {
        this.situationFamiliale = sf;
    }

    @Override
    public void setNbEnfantsACharge(int nbe) {
        this.nbEnfantsACharge = nbe;
    }

    @Override
    public void setNbEnfantsSituationHandicap(int nbesh) {
        this.nbEnfantsSituationHandicap = nbesh;
    }

    @Override
    public void setParentIsole(boolean pi) {
        this.parentIsole = pi;
    }

    @Override
    public void calculImpotSurRevenuNet() {
         simulateur.calculImpot(revenusNet, situationFamiliale, nbEnfantsACharge, nbEnfantsSituationHandicap, parentIsole);
    }

    @Override
    public int getRevenuFiscalReference() {
        return (int)simulateur.getRevenuReference();
    }

    @Override
    public int getAbattement() {
        return (int)simulateur.getAbattement();
    }

    @Override
    public double getNbPartsFoyerFiscal() {
        return simulateur.getNbParts();
    }

    @Override
    public int getImpotAvantDecote() {
        return (int)simulateur.getImpotAvantDecote();
    }

    @Override
    public int getDecote() {
        return (int)simulateur.getDecote();
    }

    @Override
    public int getImpotSurRevenuNet() {
        return (int)simulateur.getImpotNet();
    }

    @Override
    public int getRevenuNet() {
        return (int)simulateur.getRevenuNet();
    }

    @Override
    public int getRevenuReference() {
        return (int)simulateur.getRevenuReference();
    }
}
