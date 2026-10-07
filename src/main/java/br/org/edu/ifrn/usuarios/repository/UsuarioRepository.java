package br.org.edu.ifrn.usuarios.repository;

import br.org.edu.ifrn.usuarios.model.Cargo;
import br.org.edu.ifrn.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    List<Usuario> findAllByOrderByNomeAsc();

    long countByCargo(Cargo cargo);
}
