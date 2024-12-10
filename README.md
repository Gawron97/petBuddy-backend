# PetBuddy Backend

Welcome to the **PetBuddy Backend**, a Spring Boot application designed to manage offers and communication between:

- **Clients**: Individuals looking for a reliable **Pet Sitter** for their animals.
- **Caretakers**: Enthusiastic and responsible **Pet Sitters** offering their services.

This application serves as the backbone for facilitating connections, managing user interactions, and ensuring a 
seamless experience for both parties.

The project integrates with a React Web project - [PetBuddy-Frontend](https://github.com/Daarkosss/petBuddy-frontend).

---

## Table of Contents
- [Features](#features)
- [PetBuddy-Backend requirements](#petbuddy-backend-requirements)
- [Required Services Configuration](#required-services-configuration)
- [Installation](#installation)
- [Environment Variables](#environment-variables)
- [Contributors](#contributors)

---

## Features

- **Rich Offer Configuration**:
    - Caretakers can set detailed offers with availability, pricing, and care options.
    - Clients can book care services based on tailored offers.
- **Web Socket Based Chat System**: Real-time communication between caretakers and clients for seamless interaction.
- **Keycloak-Based Authentication**:
    - Registration and login for both caretakers and clients using **Keycloak**.
    - Secure authentication with **JWT tokens**.
- **Photo Management**:
    - Upload profile pictures and offer-related photos.
- **Notifications**:
    - Receive alerts for new messages and changes in care status.
- **Advanced Search and Localization**:
    - Find caretakers based on approximate location, price range, supported animal types, amenities, and more.
- **Rating System**:
    - Bayesian average rating system that values both the average score and the number of reviews.
- **Account Security**:
    - Email confirmation for account creation to ensure user authenticity.
    - User blocking system to prevent misuse and overuse.


---

## PetBuddy-Backend requirements

Ensure the following are installed:
- [Java 17+](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) (or any compatible JDK version)
- [Maven](https://maven.apache.org/) or use the included Maven wrapper

---

## Required services configuration
This project integrates with other services that needs to be separately configured:

### Keycloak and database
To make the configuration simpler, we have created a separate repository with ready to use docker-compose.yml files.
Check [PetBuddy-Deployment](https://github.com/Gawron97/PetBuddy-deployment) repository and **Launching the dependencies for PetBuddy-Backend** section.

### Firebase

To use Firebase storage, ensure the following configuration steps are completed:

1. **Configure Firebase Storage Rules**

Set the rules for Firebase storage in the Firebase console. The application needs the `READ`, `WRITE` and `DELETE`
permissions. For the development purposes and making sure the project works, it can be set to these configuration:

```
rules_version = '2';

service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write, delete: if true;
    }
  }
}
```

2. **Provide Firebase Private Key**

Obtain your Firebase private key from the Firebase Console, rename it to petbuddy-firebase-private-key.json, and place
it in the following directory at the project root:

```
./secret/petbuddy-firebase-private-key.json
```

### OpenCage Geocoding API

This step requires OpenCage Geocoding API key, that will be used as an environment variable later. In order to acquire it,
[see this tutorial](https://opencagedata.com/api#quickstart).

---

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/Gawron97/petBuddy-backend.git
   ```
2. Build the project:

Change directory into cloned project:
```bash
cd petBuddy-backend
```
   
Build and test project to verify integrity:
```bash
mvn clean install
```

---

## Environment Variables

The application requires certain environment variables to be set up in order to run the application. If you use the docker
images for Keycloak and PostgreSQL provided by [PetBuddy-Deployment](https://github.com/Gawron97/PetBuddy-deployment),
then the recommended environment variables would be:
```yml
DB_USERNAME=postgres
DB_PASSWORD=postgres
DB_URL=jdbc:postgresql://localhost:5433/petbuddy

# Logical directory structure, can be any valid UNIX-like path 
FIREBASE_PHOTO_DIRECTORY=prod 

# Needs to be provided (see previous step)
OPENCAGE_API_KEY=your_opencage_api_key
```

---

## Contributors

The PetBuddy-Backend as well as PetBuddy-Frontend was made thanks to the following contributors:
1. PetBuddy-Backend team:
- [Gawron97](https://github.com/Gawron97)
- [lukcyn](https://github.com/lukcyn)

2. PetBuddy-Frontend team:
- [Daarkosss](https://github.com/Daarkosss)
- [JakubStani](https://github.com/JakubStani)
