# Mini-Projet de Réusinage du Simulateur

## 🎯 Objectif

Réusiner une classe de simulateur existante dans un nouveau package, tout en conservant la validité des tests unitaires fonctionnels existants. L’objectif est d’obtenir un code propre, lisible, bien structuré, et conforme aux bonnes pratiques.

---

## 🧪 Prérequis

- Tous les tests unitaires fonctionnels doivent être déjà implantés et réussir.
- Ces tests constituent un filet de sécurité pour le processus de réusinage.

---

## 📁 Étapes du Projet

1. **Copie du code d'origine**
   - Copier la classe héritée du simulateur (`Simulateur`) dans un autre package.
   - Exemple de package : `com.kervaware.simulateurreusine.Simulateur`

2. **Adaptation**
   - Créer un **adaptateur** pour connecter progressivement la nouvelle classe aux tests existants.

3. **Réusinage**
   - Rendre le code :
     - Lisible avec des **concepts métier** clairs.
     - Commenté de façon **équilibrée**.
     - **Sans nombres magiques** : utiliser des constantes ou paramètres bien nommés.
     - **Sans classe avec trop de responsabilités** : découpage si nécessaire.
     - **Sans fonctions trop longues** : extraire des sous-fonctions.
     - **Modulaire et paramétrable**, notamment pour le **calcul des impôts 2025**.

4. **Validation**
   - Générer des rapports HTML montrant que :
     - ✅ 100% des **tests unitaires passent**.
     - ✅ Les tests couvrent **au moins 90% du code** du simulateur réusiné.
     - ✅ Le code passe une **analyse statique** avec **CheckStyle** et le fichier de règles `but-unicaen.xml`.

---

## 🛠 Technologies et Outils

- Java
- JUnit (ou autre framework de test unitaire)
- CheckStyle (avec fichier `iut-unicaen-check.xml` fourni)
- Génération de rapports HTML (JaCoCo ou autre outil de couverture de code)

---

## ✅ Livrables

- Nouveau package avec le simulateur réusiné
- Adaptateur pour tests
- Rapport de couverture des tests (HTML)
- Rapport CheckStyle
- README.md explicatif

---





