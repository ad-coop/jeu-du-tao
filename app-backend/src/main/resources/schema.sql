CREATE TABLE IF NOT EXISTS games (
    handle            VARCHAR(6)   PRIMARY KEY,
    password_hash     VARCHAR(255),
    created_at        TIMESTAMP    NOT NULL,
    state             VARCHAR(20)  NOT NULL,
    guardian_id       VARCHAR(36)  NOT NULL,
    magic_link_token  VARCHAR(255),
    magic_link_expiry TIMESTAMP,
    email             VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS players (
    id           VARCHAR(36)  PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    role         VARCHAR(20)  NOT NULL,
    game_handle  VARCHAR(6)   NOT NULL,
    CONSTRAINT fk_players_game FOREIGN KEY (game_handle) REFERENCES games(handle)
);
