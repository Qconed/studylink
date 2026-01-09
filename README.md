# StudyLink - Commandes essentielles

##  Commandes

### Compiler le projet apres chaque changement de CODE ET AVANT DE LANCER L'APP
```bash
mvn clean compile
```

##  Déploiement de la BD

### Initialisation (apres chaque changement de la bDD )
```bash
cd IdeaProjects/studylink
psql 'postgresql://neondb_owner:npg_ND1TJQFSuK5E@ep-cold-scene-ahba8hlp-pooler.c-3.us-east-1.aws.neon.tech:5432/neondb?sslmode=require' -f init.sql
```

### Via console Neon (web)
1. Allez sur https://console.neon.tech
2. Connectez-vous (bref faites ce qu'il vous demande ,moi je suis deja connecté mais piour vous jsp si ca sera le cas )
3. Sélectionnez le projet
4. basculez vers la carte "View database contents" , cliquez desssus et vous verrez notre base de données.

---

## 🗄️ Base de données Neon

**URL de connexion :**
```
postgresql://neondb_owner:npg_ND1TJQFSuK5E@ep-cold-scene-ahba8hlp-pooler.c-3.us-east-1.aws.neon.tech:5432/neondb?sslmode=require
```

