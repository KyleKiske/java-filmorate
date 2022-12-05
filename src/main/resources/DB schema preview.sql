-- Exported from QuickDBD: https://www.quickdatabasediagrams.com/
-- Link to schema: https://app.quickdatabasediagrams.com/#/d/5AIbzH
-- NOTE! If you have used non-SQL datatypes in your design, you will have to change these here.

CREATE TABLE "User" (
    "UserId" int64   NOT NULL,
    "Email" string   NOT NULL,
    "Login" string   NOT NULL,
    "Name" string   NOT NULL,
    "Birthday" date   NOT NULL,
    CONSTRAINT "pk_User" PRIMARY KEY (
        "UserId"
     )
);

CREATE TABLE "Friend" (
    "FriendshipId" int   NOT NULL,
    "Friend1" int64   NOT NULL,
    "Friend2" int64   NOT NULL,
    "Confirmed" bool   NOT NULL,
    CONSTRAINT "pk_Friend" PRIMARY KEY (
        "FriendshipId"
     )
);

CREATE TABLE "Film" (
    "FilmId" int   NOT NULL,
    "Name" string   NOT NULL,
    "Description" string   NOT NULL,
    "Release_date" date   NOT NULL,
    "Duration" int   NOT NULL,
    "Rating" int   NOT NULL,
    CONSTRAINT "pk_Film" PRIMARY KEY (
        "FilmId"
     )
);

CREATE TABLE "Likes" (
    "LikeId" int64   NOT NULL,
    "FilmId" int   NOT NULL,
    "UserId" int64   NOT NULL,
    CONSTRAINT "pk_Likes" PRIMARY KEY (
        "LikeId"
     )
);

CREATE TABLE "Rating" (
    "RatingId" int   NOT NULL,
    "Name" string   NOT NULL,
    CONSTRAINT "pk_Rating" PRIMARY KEY (
        "RatingId"
     )
);

CREATE TABLE "Film_Genre" (
    "RelationId" int   NOT NULL,
    "GenreId" int   NOT NULL,
    "FilmId" int   NOT NULL,
    CONSTRAINT "pk_Film_Genre" PRIMARY KEY (
        "RelationId"
     )
);

CREATE TABLE "Genre" (
    "GenreId" int   NOT NULL,
    "Name" string   NOT NULL,
    CONSTRAINT "pk_Genre" PRIMARY KEY (
        "GenreId"
     )
);

ALTER TABLE "Friend" ADD CONSTRAINT "fk_Friend_Friend1" FOREIGN KEY("Friend1")
REFERENCES "User" ("UserId");

ALTER TABLE "Friend" ADD CONSTRAINT "fk_Friend_Friend2" FOREIGN KEY("Friend2")
REFERENCES "User" ("UserId");

ALTER TABLE "Film" ADD CONSTRAINT "fk_Film_Rating" FOREIGN KEY("Rating")
REFERENCES "Rating" ("RatingId");

ALTER TABLE "Likes" ADD CONSTRAINT "fk_Likes_FilmId" FOREIGN KEY("FilmId")
REFERENCES "Film" ("FilmId");

ALTER TABLE "Likes" ADD CONSTRAINT "fk_Likes_UserId" FOREIGN KEY("UserId")
REFERENCES "User" ("UserId");

ALTER TABLE "Film_Genre" ADD CONSTRAINT "fk_Film_Genre_GenreId" FOREIGN KEY("GenreId")
REFERENCES "Genre" ("GenreId");

ALTER TABLE "Film_Genre" ADD CONSTRAINT "fk_Film_Genre_FilmId" FOREIGN KEY("FilmId")
REFERENCES "Film" ("FilmId");

