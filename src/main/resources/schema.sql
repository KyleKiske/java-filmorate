DROP TABLE IF EXISTS FRIEND;
DROP TABLE IF EXISTS LIKES;
DROP TABLE IF EXISTS USERS;
DROP TABLE IF EXISTS FILM_GENRE;
DROP TABLE IF EXISTS FILM;
DROP TABLE IF EXISTS MPA;
DROP TABLE IF EXISTS GENRE;


CREATE TABLE IF NOT EXISTS "USERS" (
    "USER_ID" bigint   GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    "EMAIL" varchar   NOT NULL,
    "LOGIN" varchar   NOT NULL,
    "NAME" varchar   NOT NULL,
    "BIRTHDAY" date   NOT NULL,
    CONSTRAINT "pk_User" PRIMARY KEY (
        "USER_ID"
     )
);

CREATE TABLE IF NOT EXISTS "FRIEND" (
    "FRIENDSHIP_ID" int   GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    "FRIEND_1" bigint   NOT NULL,
    "FRIEND_2" bigint   NOT NULL,
    "CONFIRMED" bool   DEFAULT TRUE,
    CONSTRAINT "pk_Friend" PRIMARY KEY (
        "FRIENDSHIP_ID"
     ),
    CONSTRAINT "fk_Friend_Friend1" FOREIGN KEY("FRIEND_1")
        REFERENCES "USERS" ("USER_ID"),
    CONSTRAINT "fk_Friend_Friend2" FOREIGN KEY("FRIEND_2")
        REFERENCES "USERS" ("USER_ID")
);

CREATE TABLE IF NOT EXISTS "MPA" (
    "MPA_ID" int   GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    "MPA_NAME" varchar   NOT NULL,
    CONSTRAINT "pk_Mpa" PRIMARY KEY (
        "MPA_ID"
    )
);

CREATE TABLE IF NOT EXISTS "FILM" (
    "FILM_ID" int   GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    "NAME" varchar   NOT NULL,
    "DESCRIPTION" varchar   NOT NULL,
    "RELEASE_DATE" date   NOT NULL,
    "DURATION" int   NOT NULL,
    "MPA" int   NOT NULL,
    CONSTRAINT "pk_Film" PRIMARY KEY (
        "FILM_ID"
     ),
    CONSTRAINT "fk_Film_Mpa" FOREIGN KEY("MPA")
        REFERENCES "MPA" ("MPA_ID")
);

CREATE TABLE IF NOT EXISTS "LIKES" (
    "lIKE_ID" bigint   GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    "FILM_ID" int   NOT NULL,
    "USER_ID" bigint   NOT NULL,
    CONSTRAINT "pk_Likes" PRIMARY KEY (
        "lIKE_ID"
     ),
    CONSTRAINT "fk_Likes_FilmId" FOREIGN KEY("FILM_ID")
        REFERENCES "FILM" ("FILM_ID"),
    CONSTRAINT "fk_Likes_USER_ID" FOREIGN KEY("USER_ID")
        REFERENCES "USERS" ("USER_ID")
);

CREATE TABLE IF NOT EXISTS "GENRE" (
    "GENRE_ID" int   GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    "NAME" varchar   NOT NULL,
    CONSTRAINT "pk_Genre" PRIMARY KEY (
        "GENRE_ID"
    )
);

CREATE TABLE IF NOT EXISTS "FILM_GENRE" (
    "RELATION_ID" int   GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    "GENRE_ID" int   NOT NULL,
    "FILM_ID" int   NOT NULL,
    CONSTRAINT "pk_Film_Genre" PRIMARY KEY (
        "RELATION_ID"
     ),
    CONSTRAINT "fk_Film_Genre_GenreId" FOREIGN KEY("GENRE_ID")
        REFERENCES "GENRE" ("GENRE_ID"),
    CONSTRAINT "fk_Film_Genre_FilmId" FOREIGN KEY("FILM_ID")
        REFERENCES "FILM" ("FILM_ID")
);
