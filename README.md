# EcoAlert - Backend

Backend del progetto EcoAlert, un sistema informativo per la segnalazione e la gestione di problematiche ambientali (rifiuti, vandalismo, inquinamento).

Espone API REST per la gestione degli utenti e delle segnalazioni ed è progettato per integrarsi con un’applicazione mobile sviluppata in Flutter.

---

## Panoramica

Il backend gestisce l’intero flusso applicativo lato server, occupandosi di:

* autenticazione e autorizzazione degli utenti
* gestione delle segnalazioni ambientali
* persistenza dei dati su database relazionale
* esposizione di API REST documentate tramite OpenAPI

---

## Tecnologie utilizzate

* Java 17
* Spring Boot 3
* Spring Security (JWT)
* Spring Data JPA / Hibernate
* MySQL
* OpenAPI 3

---

## Architettura

Il progetto segue una struttura a livelli tipica delle applicazioni Spring Boot:

* **controller**: gestione delle richieste HTTP e mapping degli endpoint
* **service**: logica applicativa
* **dao**: accesso ai dati (repository)
* **entity**: modelli persistenti
* **exception**: gestione centralizzata degli errori
* **config**: configurazioni applicative (es. CORS, sicurezza)

---

## Struttura del progetto

```text
EcoAlert-be/
├── src/
│   ├── main/
│   │   ├── java/com/eco/alert/ecoAlert/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── dao/
│   │   │   ├── entity/
│   │   │   ├── enums/
│   │   │   ├── exception/
│   │   │   ├── config/
│   │   │   └── EcoAlertApplication.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
├── api/
│   └── EcoAlert.yaml
├── database/
│   ├── docker-compose.yml
│   └── ecoAlert-DB.sql
├── pom.xml
└── README.md
```

---

## Installazione ed esecuzione

### Clonare il repository

```bash
git clone https://github.com/Antonio1373/EcoAlert-be.git
cd EcoAlert-be
```

---

### Configurazione del database

Configurare il database MySQL modificando il file:

```
src/main/resources/application.yml
```

Specificare:

* host
* porta
* nome database
* username
* password

---

### Avvio del progetto

Prima di avviare il server è necessario compilare il progetto utilizzando Maven:

```bash
mvn clean install
```

Successivamente è possibile avviare il server:

```bash
mvn spring-boot:run
```

Il server sarà disponibile all’indirizzo:

```
http://localhost:8080
```

---

## Database

Lo schema del database è disponibile in:

```
database/ecoAlert-DB.sql
```

È inoltre disponibile un file `docker-compose.yml` per avviare rapidamente un'istanza del database.

---

## Documentazione API

La specifica OpenAPI è disponibile in:

```
api/EcoAlert.yaml
```

Questa viene utilizzata anche per la generazione automatica del client nel frontend.

---

## Endpoint principali

| Metodo | Endpoint           | Descrizione            |
| ------ | ------------------ | ---------------------- |
| POST   | /api/auth/login    | Autenticazione utente  |
| POST   | /api/auth/register | Registrazione utente   |
| GET    | /api/user/{id}     | Recupero dati utente   |
| POST   | /api/segnalazioni  | Creazione segnalazione |

---

## Integrazione con il frontend

Il backend è progettato per funzionare con il frontend Flutter disponibile al seguente repository:

https://github.com/Antonio1373/EcoAlert-fe

---

## Autore

Antonio Granato
Laureando in Informatica

---

## Licenza

Progetto sviluppato a scopo didattico.
