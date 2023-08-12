package com.gpse.basis;

import com.gpse.basis.domain.SesamUser;
import com.gpse.basis.domain.repository.UserRepository;
import com.gpse.basis.domain.service.UserService;
import com.gpse.basis.web.UserController;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCollection;

@SpringBootTest
class UserControllerTest {


	@Autowired
	private UserController userController;

	@Autowired
	private UserRepository userRepository;

	@Transactional
	@Test
	void showUserTest() {
		// Create a user and save it to the database
		SesamUser user = new SesamUser("John", "Doe", "johndoe@example.com", "password");
		userRepository.save(user);
		user.setActivated(true);
		user.setRegistered(true);

		List<SesamUser> sesamUsers = userController.showUsers();

		// Check if the user is in the list of users
		for(SesamUser sesamUser : sesamUsers) {
			if(sesamUser.getEmail().equals(user.getEmail())) {
				assertThat(sesamUser).isEqualTo(user);
			}
		}
		userRepository.delete(user);
	}

	@Test
	void showUnregisteredUserTest(){
		// Create a user and save it to the database
		SesamUser user = new SesamUser("John", "Doe", "johndoe@example.com", "password");
		userRepository.save(user);

		List<SesamUser> unregisteredUsers = userController.showUnregisteredUsers();

		for (SesamUser sesamUser : unregisteredUsers) {
			if(sesamUser.getEmail().equals(user.getEmail())) {
				assertThat(sesamUser).isEqualTo(user);
			}
		}
		userRepository.delete(user);
	}

}