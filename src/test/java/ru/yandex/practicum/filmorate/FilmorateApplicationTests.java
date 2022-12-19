package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.dao.DBFilmRepository;
import ru.yandex.practicum.filmorate.dao.DBGenreRepository;
import ru.yandex.practicum.filmorate.dao.DBMPARepository;
import ru.yandex.practicum.filmorate.dao.DBUserRepository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2, replace = AutoConfigureTestDatabase.Replace.AUTO_CONFIGURED)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FilmorateApplicationTests {
	private final DBUserRepository userRepository;
	private final DBMPARepository mpaRepository;
	private final DBGenreRepository genreRepository;
	private final DBFilmRepository filmRepository;

	@Test
	@Order(1)
	public void testFindUserById() {
		userRepository.createUser(User.builder()
						.login("test")
						.name("TestName")
						.email("test@testmail.tv")
						.birthday(LocalDate.parse("1965-01-03"))
				.build());
		Optional<User> userOptional = userRepository.getUserById(1L);

		assertThat(userOptional)
				.isPresent()
				.hasValueSatisfying(user ->
						assertThat(user).hasFieldOrPropertyWithValue("id", 1L)
				);
	}

	@Test
	@Order(2)
	public void testGetAllUsers(){
		userRepository.createUser(createUser("secondUser"));
		userRepository.createUser(createUser("thirdUser"));
		List<Optional<User>> users = userRepository.getUsers();

		assertThat(users)
				.isNotNull();
		Assertions.assertEquals(3, users.size());
	}

	@Test
	@Order(3)
	public void testAddFriend(){
		userRepository.addFriend(1L, 2L);
		List<Optional<User>> friendsOne = userRepository.getFriendsById(1L);
		List<Optional<User>> friendsTwo = userRepository.getFriendsById(2L);

		Assertions.assertEquals(1, friendsOne.size());
		Assertions.assertEquals(0, friendsTwo.size());
	}

	@Test
	@Order(4)
	public void testRemoveFriend(){
		userRepository.deleteFriend(1L, 2L);
		List<Optional<User>> friendsOne = userRepository.getFriendsById(1L);
		List<Optional<User>> friendsTwo = userRepository.getFriendsById(2L);

		Assertions.assertEquals(0, friendsOne.size());
		Assertions.assertEquals(0, friendsTwo.size());
	}

	@Test
	@Order(5)
	public void testMutualFriend(){
		userRepository.createUser(createUser("amogus"));
		userRepository.addFriend(1L, 2L);
		userRepository.addFriend(1L, 2L);
		userRepository.addFriend(1L, 4L);
		userRepository.addFriend(3L, 1L);
		userRepository.addFriend(3L, 2L);
		userRepository.addFriend(3L, 4L);
		List<Optional<User>> friendsOne = userRepository.getFriendsById(1L);
		List<Optional<User>> friendsTwo = userRepository.getFriendsById(3L);
		Assertions.assertEquals(3, friendsOne.size());
		Assertions.assertEquals(3, friendsTwo.size());

		List<Optional<User>> mutual = userRepository.getCommonFriends(1L, 3L);
		Assertions.assertEquals(2, mutual.size());
	}

	@Test
	@Order(6)
	public void testRedactUser(){
		Optional<User> userBefore = userRepository.getUserById(1L);
		String loginBefore = "";
		String loginAfter = "";
		if (userBefore.isPresent()){
			loginBefore = userBefore.get().getLogin();
			userBefore.get().setLogin("REDACTED");
			userRepository.updateUser(userBefore.get());
			loginAfter = userBefore.get().getLogin();
		}
		Assertions.assertNotEquals(loginBefore, loginAfter);
	}

	@Test
	@Order(7)
	public void testFindMPA(){
		MPA mpa = mpaRepository.getMPA(1);
		Assertions.assertEquals("G", mpa.getName());
	}

	@Test
	@Order(8)
	public void testFindAllMPA(){
		List<MPA> mpaList = mpaRepository.findAll();
		Assertions.assertEquals(5, mpaList.size());
	}

	@Test
	@Order(9)
	public void testFindGenre(){
		Genre genre = genreRepository.getGenre(1);
		Assertions.assertEquals("Комедия", genre.getName());
	}

	@Test
	@Order(10)
	public void testFindAllGenres(){
		List<Genre> genreList = genreRepository.findAll();
		Assertions.assertEquals(6, genreList.size());
	}

	@Test
	@Order(11)
	public void testFindFilm(){
		filmRepository.createFilm(createFilm("The Movie"));
		Optional<Film> filmOptional = filmRepository.getFilmById(1);

		assertThat(filmOptional)
				.isPresent()
				.hasValueSatisfying(film ->
						assertThat(film).hasFieldOrPropertyWithValue("id", 1)
				);
	}

	@Test
	@Order(12)
	public void testFindAllFilm(){
		filmRepository.createFilm(createFilm("The Movie2"));
		List<Optional<Film>> films = filmRepository.getFilms();

		assertThat(films)
				.isNotNull();
		Assertions.assertEquals(2, films.size());
	}

	@Test
	@Order(13)
	public void testFindMostPopularFilm(){
		filmRepository.createFilm(createFilm("The Movie3"));
		filmRepository.likeFilm(1,1L);
		filmRepository.likeFilm(1,2L);
		filmRepository.likeFilm(3,1L);
		List<Optional<Film>> films = filmRepository.getPopular(2);

		assertThat(films)
				.isNotNull();
		Assertions.assertTrue(films.get(0).isPresent());
		Assertions.assertTrue(films.get(1).isPresent());
		Assertions.assertEquals(2, films.size());
		Assertions.assertEquals(1 ,films.get(0).get().getId());
		Assertions.assertEquals(3 ,films.get(1).get().getId());

		filmRepository.unlikeFilm(1,1L);
		filmRepository.unlikeFilm(1,2L);
		filmRepository.likeFilm(3,2L);
		filmRepository.likeFilm(2,1L);
		filmRepository.likeFilm(2,2L);
		filmRepository.likeFilm(2,3L);
		films = filmRepository.getPopular(3);
		Assertions.assertTrue(films.get(0).isPresent());
		Assertions.assertTrue(films.get(1).isPresent());
		Assertions.assertEquals(3, films.size());
		Assertions.assertEquals(2 ,films.get(0).get().getId());
		Assertions.assertEquals(3 ,films.get(1).get().getId());
	}

	@Test
	@Order(14)
	public void testRedactFilm(){
		Optional<Film> filmOptional = filmRepository.getFilmById(1);
		String nameBefore = "";
		String nameAfter = "";
		if (filmOptional.isPresent()){
			nameBefore = filmOptional.get().getName();
			filmOptional.get().setName("REDACTED");
			filmRepository.updateFilm(filmOptional);
			nameAfter = filmOptional.get().getName();
		}
		Assertions.assertNotEquals(nameBefore, nameAfter);
	}

	private User createUser(String login){
		return User.builder()
				.login(login)
				.name("TestName")
				.email("test@testmail.tv")
				.birthday(LocalDate.parse("1965-01-03"))
				.build();
	}

	private Film createFilm(String name){
		return Film.builder()
				.name(name)
				.description("TestDescription")
				.releaseDate(LocalDate.parse("1965-01-03"))
				.duration(130)
				.mpa(mpaRepository.getMPA(1))
				.build();
	}
}