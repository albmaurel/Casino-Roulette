# Distributed Casino Roulette System

## Project Overview

This project implements a distributed system simulating a casino roulette game. The system consists of a central server where multiple clients can log in, either by creating new accounts or by signing into existing ones. Once logged in, clients can join existing game rooms or create new ones, which will then be available for others to join. Each room is associated with a roulette game that operates on a timer: 35 seconds for placing bets and 15 seconds for displaying results.

Every client starts with a balance of 10,000 units of virtual currency, which cannot be replenished. The balance is saved upon logging out. After each round in a roulette room, a dynamic leaderboard is generated, showcasing the top players based on their winnings.

## Key Features

- **Central Server:** Manages client sessions and game rooms.
- **User Authentication:** Clients can create accounts and log in.
- **Room Management:** Clients can join existing rooms or create new ones.
- **Roulette Game:** Each room hosts a roulette game with a 35-second betting window and a 15-second results display.
- **Persistent Balances:** Client balances are saved on logout.
- **Dynamic Leaderboards:** Leaderboards are updated after each round in each room.

## System Architecture

### Server
- **Main Class:** `ServidorPrincipal.java`
- **Handler Class:** `GestionarServidor`
  - Manages client connections and room creation.
- **Room Management:** `ServidorRuleta` class
  - Each room is managed by `GestionarRuleta`.

### Client
- **Client GUI:** `Cliente_GUI`
  - Each client runs a separate instance of this class for independent sessions.

## Usage

1. **Server Setup:** Ensure the central server (`ServidorPrincipal.java`) is running and active at all times.
2. **Client Interaction:** Clients use the `Cliente_GUI` interface to:
   - Log in or create a new account.
   - Join an existing room or create a new one.
   - Place bets and view results.
3. **Game Flow:**
   - Once in a room, clients have 35 seconds to place bets.
   - After the timer ends, results are shown for 15 seconds.
   - Leaderboards are updated dynamically after each round.

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/albmaurel/Casino-Roulette.git
   ```
2. Compile the server and client classes:
   ```bash
   javac ServidorPrincipal.java Cliente_GUI.java
   ```
3. Run the server:
   ```bash
   java ServidorPrincipal
   ```
4. Run client instances:
   ```bash
   java Cliente_GUI
   ```
