# EticPlus-POC API Documentation

This documentation provides detailed information about the API endpoints available in the EticPlus-POC project.

## Authentication

### Register User

- **URL:** `/api/register`
- **Method:** `POST`
- **Description:** Registers a new user.
- **Request Body:**
  ```json
  {
    "storeName": "string",
    "password": "string",
    "email": "string",
    "category": "string",
    "packageType": "string"
  }
- **Response:**
  ```json
  {
  "message": "User registered successfully."
  }
### Login User

- **URL:** `/api/login`
- **Method:** `POST`
- **Description:** Authenticates a user and returns a JWT token.
- **Request Body:**
  ```json
  {
  "username": "string",
  "password": "string"
  }
- **Response:**
  ```json
  {
  "jwt": "string"
  }
### Get User Profile

- **URL:** `/api/profile`
- **Method:** `GET`
- **Description:** Retrieves the profile details of the logged-in user.
- **Headers:** Authorization: Bearer JWT Token
- **Response:**
  ```json
  {
  "storeName": "string",
  "password": "string",
  "category": "string",
  "packageType": "string"
  }
### Update User Profile

- **URL:** `/api/updateProfile`
- **Method:** `PUT`
- **Description:** Updates the profile details of the logged-in user.
- **Headers:** Authorization: Bearer JWT Token
- **Request Body:**
  ```json
  {
  "storeName": "string",
  "password": "string",
  "category": "string",
  "packageType": "string"
  }
- **Response:**
  ```json
  {
  "message": "Profile updated successfully."
  }
### Get Home Page Plugins

- **URL:** `/api/home`
- **Method:** `GET`
- **Description:** Retrieves the plugins associated with the logged-in user's store.
- **Headers:** Authorization: Bearer JWT Token
- **Response:**
  ```json
  [
  {
    "pluginName": "string",
    "status": "active/inactive"
  }
  ]
### Toggle Plugin Status

- **URL:** `/api/togglePlugin`
- **Method:** `POST`
- **Description:** Toggles the status (active/inactive) of a plugin for the logged-in user's store.
- **Headers:** Authorization: Bearer JWT Token
- **Request Params:** pluginName=string
- **Response:**
  ```json
  {
  "message": "Plugin status updated."
  }
### Get All Store Categories

- **URL:** `/api/categories`
- **Method:** `GET`
- **Description:** Retrieves a list of all available store categories.
- **Response:**
  ```json
  [
  {
    "id": "string",
    "name": "string"
  }
  ]
### Delete Account

- **URL:** `/api/deleteAccount`
- **Method:** `POST`
- **Description:** Deletes the account of the logged-in user.
- **Headers:** Authorization: Bearer JWT Token
- **Response:**
  ```json
  {
  "message": "Account has been deleted."
  }
### Logout

- **URL:** `/api/logout`
- **Method:** `POST`
- **Description:** Logs out the currently authenticated user.
- **Response:**
  ```json
  {
  "message": "You have been successfully logged out."
  }
### Get All Action Logs

- **URL:** `/api/actions`
- **Method:** `GET`
- **Description:** Retrieves a list of all action logs.
- **Response:**
  ```json
  [
  {
    "id": "string",
    "action": "string",
    "timestamp": "date-time",
    "storeName": "string"
  }
  ]
### Get Action Logs by Store Name
- **URL:** `/api/actions/{storeName}`
- **Method:** `GET`
- **Description:** Retrieves action logs filtered by store name.
- **Response:**
  ```json
  [
  {
    "id": "string",
    "action": "string",
    "timestamp": "date-time",
    "storeName": "string"
  }
  ]  
