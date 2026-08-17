# Valeur foncière forestière (valeur vénale des parcelles boisées)
**Domaine** : docs/recherche/02_marche_prix/
**Date de recherche** : 2026-07-02
**Agent** : valeur foncière forestière (SAFER / Fransylva / DVF)

## Contexte et périmètre

GeoSylva calcule aujourd'hui la **valeur du bois sur pied** (cubage × prix €/m³ par qualité,
cf. `PriceCalculator`, `01_cubage_volume/05_normes_qualite_bois.md`). Cette recherche porte sur
un besoin différent et complémentaire : la **valeur vénale du foncier forestier lui-même**
(le terrain + le peuplement qui s'y trouve, dans une logique de transaction immobilière —
achat/vente/succession/expertise patrimoniale), qui intéresse un usage pro (expert forestier,
gestionnaire, propriétaire) au-delà du seul prix du bois. C'est l'opportunité déjà repérée en
`docs/REFERENTIELS_FORESTIERS_EXTERNES.md` §5 et action prioritaire #11 de
`RESEARCH_OPPORTUNITIES.md` (« Intégrer données SAFER pour un module valeur foncière »). Ce
document complète/affine le §5 existant, ne le réécrit pas.

## 1. Sources identifiées

| Source | Type | Fiabilité | URL | Date/version |
|---|---|---|---|---|
| SAFER / Groupe Safer-SSP — étude annuelle « Le prix des terres », chapitre « Le marché des forêts » | officielle (mission de service public, données notariales obligatoires) | Élevée pour la collecte, **modérée** pour la moyenne nationale communiquée (cf. §3) | https://www.safer.fr/app/uploads/2025/10/FORETS.pdf | Édition 2025, données 2024 (mise à jour 2025-05, republiée 2025-10) |
| Le-prix-des-terres.fr — cartographie interactive gratuite | officielle (portail SAFER) | Élevée (source primaire) | https://www.le-prix-des-terres.fr/carte/foret/ | Consulté 2026-07-02 |
| Barème indicatif de la valeur vénale moyenne des **terres agricoles** — décision ministérielle annuelle, JORF | officielle (Ministère de l'Agriculture, publiée au JO) | Élevée mais **hors périmètre forêt** (cf. limites) | https://www.legifrance.gouv.fr/jorf/id/JORFTEXT000052153430 | Décision du 26/08/2025, données 2024 |
| Cerema Datafoncier — DVF géolocalisées (open data) | officielle (DGFiP/DGALN/Cerema, via data.gouv.fr) | Élevée (donnée brute notariale) | https://www.data.gouv.fr/datasets/demandes-de-valeurs-foncieres-geolocalisees | Mise à jour continue, 5 dernières années glissantes |
| Cerema Datafoncier — DV3F / DVF+ (modèle enrichi) | officielle | Élevée, mais **accès restreint** (cf. limites) | https://doc-datafoncier.cerema.fr/doc/guide/dv3f/notions-avancees-sur-les-parcelles | Consulté 2026-07-02 |
| Pierre Aussedat (cabinet d'expertise/transaction forestière) — analyse critique + « indice propre » | commerciale/tierce | Moyenne (avis de professionnel, méthodologie propre non publiée en détail) | https://pierreaussedat.com/le-prix-de-la-foret-francaise/ | Article du 25/06/2025 |
| Fransylva (syndicats régionaux PACA, Limousin) — documents pédagogiques « Combien vaut ma forêt » | commerciale/tierce (syndicat professionnel) | Faible en tant que source chiffrée nationale (pas d'indice quantifié publié) | http://fransylva-paca.fr/wp/wp-content/uploads/2018/02/Prix-for%C3%AAt-Fransylva-83.pdf | 2018, réaffirmé 2022 (Limousin) |

## 2. Données détaillées

### 2.1 SAFER — « Le prix des terres 2024 », marché des forêts (référence principale)

Chiffres nationaux 2024 (source primaire, PDF officiel + synthèse Safer Grand Est) :

| Indicateur | Valeur 2024 | Évolution vs 2023 |
|---|---|---|
| Prix moyen national des forêts | **4 850 €/ha** | +2,2 % |
| Fourchette où se situent 90 % des transactions | 730 – 14 570 €/ha | — |
| Prix moyen des massifs > 25 ha (record) | 5 960 €/ha | +0,9 % |
| Nombre de transactions | 21 860 | — |
| Surface totale échangée | 148 700 ha | +1,6 % (surface) |
| Montant total des transactions | 2,02 milliards € | +4,8 % (valeur) |
| Part de marché des massifs > 100 ha | 25 % des surfaces vendues | +13,8 % en surface |
| Part de marché des bois < 10 ha | 37 % des surfaces vendues | +0,6 % en surface |

**Méthode SAFER** : moyenne calculée sur l'ensemble des projets de vente de surfaces boisées
notifiées par les notaires aux SAFER (obligation légale), quel que soit l'acquéreur. Le prix
« moyen » communiqué **écarte certains éléments annexes** (bâti, terres agricoles associées sur
des propriétés mixtes) selon l'analyse de tiers (cf. §3) — la méthode exacte de pondération/
exclusion n'est **pas détaillée dans le document public consulté**
`[À VÉRIFIER MANUELLEMENT auprès de la SAFER]`.

**Granularité géographique** : le rapport et la carte interactive découpent la France en
**7 grandes régions forestières** (regroupements de sylvoécorégions IGN) : Alpes-Méditerranée-
Pyrénées, Corse, Est, Massif Central, Nord-Bassin Parisien, Ouest, Sud-Ouest. C'est **beaucoup
plus grossier** que le découpage par « petites régions agricoles » disponible pour les terres
labourables/prés (résolution quasi-départementale, cf. exemple SAFER IDF département par
département dans le PDF `saferidf.fr`). Il n'existe **pas** de prix communal ou parcellaire
publié par la SAFER pour les forêts.

**Accès** : gratuit, sans clé ni inscription — rapport PDF téléchargeable + carte interactive
(`https://www.le-prix-des-terres.fr/carte/foret/`). Pas d'API structurée identifiée (pas de JSON/
CSV téléchargeable constaté pour le module forêt, contrairement à DVF).

### 2.2 Barème indicatif officiel (JORF) — hors périmètre forêt

Le barème publié annuellement au Journal Officiel (« Décision portant fixation du barème
indicatif de la valeur vénale moyenne des terres agricoles ») sert de référence légale/fiscale
(baux ruraux, droits de mutation à titre gratuit). D'après les tableaux identifiés dans les
extraits consultés (terres labourables, vignes), **ce barème officiel semble structuré autour des
terres agricoles et vignes, et ne comporte pas de tableau dédié « forêts »**
`[À VÉRIFIER MANUELLEMENT — accès complet au PDF Légifrance non obtenu, HTTP 403 lors du test]`.
Si confirmé, cela signifie que **la seule référence quasi-officielle pour la valeur foncière
forestière reste l'étude SAFER « Le prix des terres »**, qui est une étude de marché (statistique
descriptive), pas un barème à valeur légale/fiscale comme celui des terres agricoles.

### 2.3 Analyses critiques indépendantes (Pierre Aussedat)

Un expert forestier spécialisé dans la transaction de grands massifs conteste la représentativité
du chiffre SAFER de 4 850 €/ha :
- Calcul de la **moyenne brute** (montant total / surface totale) = 2,02 Md€ / 148 700 ha =
  **13 585 €/ha**, soit **~2,8× le chiffre officiel communiqué**.
- Hypothèse avancée (non confirmée officiellement) : le chiffre SAFER retirerait la valeur
  estimée des maisons pour les ventes mixtes (~195 000 €, prix moyen SAFER des « maisons à la
  campagne ») et exclurait certaines transactions extrêmes.
- Constat terrain : des transactions à 20 000, 30 000, voire > 50 000 €/ha pour des forêts de
  qualité sont « de plus en plus courantes », et la valeur du stock de bois peut représenter
  jusqu'à 90 % du prix d'une forêt de production — avec un facteur ×1 à ×3 de variation entre
  parcelles selon les pratiques sylvicoles.
- Ce cabinet a développé un **indice propriétaire non public** combinant rentabilité, valeur du
  stock de bois et analyse de marché régionale, portant sur >75 % des transactions >100 ha en
  France — **source commerciale, méthodologie non vérifiable**, utile comme point de comparaison
  qualitatif uniquement.

`[À VÉRIFIER MANUELLEMENT]` : cette critique n'est pas une contre-expertise scientifique publiée,
mais l'avis argumenté d'un acteur commercial concurrent/partenaire de la SAFER sur son propre
marché (massifs > 100 ha) — à recouper avec d'autres avis avant intégration dans l'app.

### 2.4 Fransylva — pas d'indice national quantifié identifié

Les syndicats régionaux Fransylva (PACA, Limousin, Poitou-Charentes) publient des documents
pédagogiques (« Combien vaut ma parcelle boisée ? ») expliquant les facteurs de valeur (essence,
âge, densité, fertilité, accès, foncier, zonage) mais **annoncent un référentiel régional futur
« qui sera proposé à l'avenir »** — au moment de la recherche, aucun barème chiffré national ou
régional Fransylva accessible en ligne n'a été trouvé. L'accompagnement individuel proposé par
Fransylva pour l'estimation d'une parcelle est un **service payant** (mise en relation avec un
expert forestier), pas une donnée ouverte. Fransylva mentionne aussi une estimation de **300 à
900 €/ha/an** de « valeur annuelle » de services non marchands (biodiversité, paysage, etc.),
attribuée à « divers auteurs » sans référence précise
`[À VÉRIFIER MANUELLEMENT — source primaire non citée dans le document Fransylva-PACA]`.

**Conclusion** : contrairement à ce que suggérait l'intitulé de la mission, il n'existe **pas**
d'« indice Fransylva » chiffré et publié équivalent au baromètre SAFER. Ce point est un vrai
constat de lacune, pas une omission de recherche.

### 2.5 DVF / DVF+ (Cerema-DGFiP) — le foncier forestier EST identifiable, et en open data gratuit

Contrairement à l'hypothèse implicite de `RESEARCH_OPPORTUNITIES.md` (DVF listé sans précision),
le jeu de données **« Demandes de valeurs foncières géolocalisées »** (data.gouv.fr, Licence
Ouverte 2.0, gratuit, sans inscription) contient directement les champs :
- `code_nature_culture` / `nature_culture`
- `code_nature_culture_speciale` / `nature_culture_speciale`

Le référentiel des « natures de culture » de la DGFiP identifie la classe **dcnt05 = Bois**, qui
regroupe les codes suivants (documentation Cerema Datafoncier) :

| Code | Libellé |
|---|---|
| B | Bois |
| BF | Futaies feuillues |
| BM | Futaies mixtes |
| BO | Oseraies |
| BP | Peupleraies |
| BR | Futaies résineuses |
| BS | Taillis sous futaie |
| BT | Taillis simples |

Ces codes permettent de **filtrer les transactions DVF portant sur des parcelles boisées**, à
l'échelle de la mutation individuelle géolocalisée (parcelle cadastrale, commune, date, prix,
surface) — une granularité **bien plus fine** que les 7 régions forestières SAFER.

**Limites importantes** :
- Le champ « nature de culture » de DVF grand public reflète la **subdivision fiscale MAJIC**,
  mise à jour prioritairement lors de nouvelles constructions — les surfaces boisées peuvent donc
  apparaître comme agricoles et inversement (donnée pas toujours à jour de l'usage réel du sol).
  Documentation Cerema : « les changements d'occupation aboutissant à une vocation agricole ou
  naturelle sont peu suivis ».
- DVF exclut historiquement l'Alsace-Moselle et Mayotte (régime de publicité foncière différent).
- Le modèle **DVF+/DV3F enrichi** (indicateurs de marché calculés, agrégats par zone) reste
  réservé aux « bénéficiaires » (collectivités, État, SAFER, EPF, organismes de recherche...) via
  convention avec la DGALN/Cerema — **non accessible à une entreprise privée comme GeoSylva**
  sans mission de service public. Seul le DVF brut géolocalisé (avec nature de culture) est
  librement exploitable.
- Comme pour les terres agricoles, une transaction « bois » dans DVF peut être une vente mixte
  (parcelle avec bâti) — nécessite un filtrage/nettoyage pour isoler les ventes de forêt pure,
  travail de data engineering non trivial mais réalisable en interne.

### 2.6 Cas d'usage GeoSylva envisageable

Pour estimer la **valeur vénale d'une parcelle forestière** (pas seulement la valeur du bois),
deux approches complémentaires, aucune n'étant suffisante seule :

1. **Approche « prix de marché » (comparables)** : croiser le polygone de la parcelle (déjà
   disponible dans GeoSylva via cadastre IGN) avec :
   - le prix moyen régional SAFER (région forestière, granularité grossière mais officielle et
     mise à jour annuellement, gratuite) comme ordre de grandeur macro ;
   - des transactions DVF filtrées sur `nature_culture` = codes bois (B/BF/BM/BO/BP/BR/BS/BT)
     dans la même commune/canton pour affiner localement — sous réserve du travail de nettoyage
     mentionné ci-dessus.
2. **Approche « valeur intrinsèque »** (déjà partiellement couverte par GeoSylva) : valeur du bois
   sur pied (cubage × prix qualité) + valeur du fonds (sol nu, sans peuplement, approximée par le
   bas de la fourchette SAFER) — cohérent avec le constat d'Aussedat que le bois peut représenter
   jusqu'à 90 % du prix d'une forêt de production.

Aucune de ces deux approches ne remplace une expertise de terrain (inventaire en plein) : le
même article Aussedat illustre un écart de 5,6 M€ (estimé par le gestionnaire) à 8 M€ (après
inventaire complet) sur un même massif — soit +43 %. **Tout module « valeur foncière » GeoSylva
devrait afficher explicitement une fourchette indicative et une clause de non-garantie**, jamais
un chiffre unique présenté comme fiable à l'hectare près.

## 3. Comparatif / analyse critique

| Source | Granularité | Coût | Fiabilité officielle | Limite principale |
|---|---|---|---|---|
| SAFER « Le prix des terres » | 7 régions forestières | Gratuit | Officielle, mais moyenne contestée par des pros | Pas d'API, pas de fichier structuré, résolution grossière |
| DVF géolocalisées (nature_culture bois) | Parcelle/commune | Gratuit | Officielle (donnée brute notariale) | Nature de culture pas toujours à jour ; ventes mixtes non isolées automatiquement |
| DVF+/DV3F enrichi | Commune/zone, indicateurs calculés | Gratuit mais accès restreint | Officielle | Non accessible à une entreprise privée sans mission de service public |
| Barème JORF terres agricoles | Département/petite région agricole | Gratuit | Officielle, valeur légale/fiscale | Ne couvre pas les forêts (constat à confirmer) |
| Fransylva | Aucun barème chiffré public trouvé | — | Syndicat professionnel | Pas de donnée exploitable en l'état |
| Pierre Aussedat (indice propriétaire) | Massifs > 100 ha | Gratuit (article), méthode non publiée | Commerciale | Pas reproductible, pas une source de données brutes |

**Conclusion de l'analyse** : il n'existe en France **aucune source ouverte, structurée et
granulaire** de la valeur foncière forestière comparable à ce qui existe pour les terres
agricoles (barème officiel départemental). La SAFER reste la référence macro la plus citée mais
sa moyenne nationale est **elle-même activement critiquée par des professionnels du secteur** pour
sa faible représentativité des transactions réelles (écart observé jusqu'à ×2,8 selon le mode de
calcul). Tout affichage dans GeoSylva doit refléter cette incertitude plutôt que de donner une
fausse impression de précision.

## 4. Recommandation pour GeoSylva

**Priorité : Medium** (déjà identifiée comme item #11 dans `RESEARCH_OPPORTUNITIES.md`,
confirmée pertinente mais plus complexe qu'un simple import de barème).

1. **Ne pas intégrer le chiffre SAFER seul comme valeur affichée à l'hectare.** Si un module
   « valeur foncière indicative » est développé (nouveau composant, ex. `LandValueEstimator`,
   distinct de `PriceCalculator` qui reste dédié à la valeur du bois), afficher **une fourchette**
   (ex. bas de fourchette régionale SAFER / haut de fourchette) plutôt qu'un point unique, avec un
   avertissement explicite (« estimation indicative de marché, ne remplace pas une expertise »).
2. **Stocker un barème régional SAFER statique** (7 régions forestières + prix moyen, mis à jour
   manuellement une fois par an à la publication du rapport de mai) — faible effort, cohérent
   avec l'approche déjà utilisée pour `RegionalPricePresets.kt` (prix du bois).
3. **Évaluer séparément** l'intégration du filtrage DVF géolocalisées par `nature_culture` bois
   (dataset déjà listé dans `RESEARCH_OPPORTUNITIES.md` comme « déjà intégré » pour le foncier
   général — vérifier si le filtre nature de culture bois est effectivement exploité côté backend
   GeoSylva, ce qui ne semblait pas être le cas au moment de cette recherche). Ce travail est plus
   lourd (nettoyage des ventes mixtes, agrégation par commune) et devrait être une tâche dédiée,
   pas un simple ajout de barème.
4. **Ne pas chercher d'API Fransylva** : aucune donnée chiffrée exploitable identifiée à ce jour.
5. **Documentation utilisateur** : si un tel module est livré, citer explicitement la source
   (« Groupe Safer – le-prix-des-terres.fr », obligation de citation rappelée par la SAFER
   elle-même) et le caractère indicatif du chiffre.

## 5. Limites et points à vérifier manuellement

- Le contenu exact du barème indicatif officiel JORF (décision du 26/08/2025) n'a pas pu être
  consulté en entier (blocage HTTP 403 sur Légifrance lors du test) — **à vérifier manuellement**
  si une section « forêts » y figure malgré tout (peu probable au vu des extraits obtenus, mais
  non confirmé à 100 %).
- La méthode exacte de calcul de la moyenne SAFER « 4 850 €/ha » (exclusions/pondérations
  éventuelles) n'est pas détaillée dans le PDF public consulté — la présomption de recalcul de
  moyenne brute à 13 585 €/ha (Pierre Aussedat) est une **estimation tierce**, pas une donnée
  SAFER officielle, marquée comme telle.
- Aucun test d'accès API réel n'a été effectué sur le portail Datafoncier (accès nécessitant un
  compte « bénéficiaire » avec acte d'engagement, hors de portée d'un test anonyme) — l'analyse
  des conditions d'accès est basée sur la documentation publique du Cerema, pas sur un test direct.
- Le chiffrage Fransylva « 300 à 900 €/ha/an de valeur de services non marchands » n'est pas
  sourcé avec une référence primaire identifiable dans le document PACA consulté — à traiter comme
  non fiable en l'état.
- Les données SAFER par région forestière détaillée (les 7 sous-rapports régionaux, ex.
  Alpes-Méditerranée-Pyrénées) n'ont pas été extraites chiffre par chiffre dans cette recherche
  (seul le PDF national FORETS.pdf a été partiellement exploité) — à faire si le module régional
  est développé.
