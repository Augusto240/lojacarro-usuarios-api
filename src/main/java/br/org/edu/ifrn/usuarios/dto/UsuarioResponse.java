package br.org.edu.ifrn.usuarios.dto;

import br.org.edu.ifrn.usuarios.model.Usuario;

public record UsuarioResponse(Long id, String nome, String cargo) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getCargo().name());
    }
}
