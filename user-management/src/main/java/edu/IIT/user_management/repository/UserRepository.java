package edu.IIT.user_management.repository;

import edu.IIT.user_management.dto.UserDTO;
import edu.IIT.user_management.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

}
