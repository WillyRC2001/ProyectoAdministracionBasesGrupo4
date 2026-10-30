package com.grupo4.proyectoadministracionbases.service;

import com.grupo4.proyectoadministracionbases.dto.auditoria.AsignacionRolDTO;
import com.grupo4.proyectoadministracionbases.dto.auditoria.HallazgoAuditoriaDTO;
import com.grupo4.proyectoadministracionbases.dto.auditoria.ObjetoInvalidoDTO;
import com.grupo4.proyectoadministracionbases.dto.auditoria.PanelAuditoriaDTO;
import com.grupo4.proyectoadministracionbases.dto.auditoria.PrincipalDTO;
import com.grupo4.proyectoadministracionbases.repository.AuditoriaRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditoriaService {

    private final AuditoriaRepository repository;

    public AuditoriaService(AuditoriaRepository repository) {
        this.repository = repository;
    }

    public PanelAuditoriaDTO construirPanel() {
        List<PrincipalDTO> logins = repository.listarLogins();
        List<PrincipalDTO> usuarios = repository.listarUsuariosBaseDatos();
        List<AsignacionRolDTO> roles = repository.listarAsignacionesRoles();
        List<ObjetoInvalidoDTO> objetos = repository.listarObjetosInvalidos();

        return new PanelAuditoriaDTO(
                repository.obtenerNombreBaseDatos(),
                logins,
                usuarios,
                roles,
                repository.listarPermisos(),
                objetos,
                generarHallazgos(logins, roles, objetos));
    }

    static List<HallazgoAuditoriaDTO> generarHallazgos(List<PrincipalDTO> logins,
                                                       List<AsignacionRolDTO> roles,
                                                       List<ObjetoInvalidoDTO> objetos) {
        List<HallazgoAuditoriaDTO> hallazgos = new ArrayList<>();

        String sysadmins = miembrosDe(roles, "SERVIDOR", "sysadmin");
        if (!sysadmins.isEmpty()) {
            hallazgos.add(new HallazgoAuditoriaDTO(
                    "ADVERTENCIA",
                    "Cuentas con rol sysadmin",
                    "Tienen control total del servidor: " + sysadmins
                            + ". Conviene limitarlas a las cuentas administrativas necesarias."));
        }

        String propietarios = miembrosDe(roles, "BASE DE DATOS", "db_owner");
        if (!propietarios.isEmpty()) {
            hallazgos.add(new HallazgoAuditoriaDTO(
                    "ADVERTENCIA",
                    "Usuarios con rol db_owner en la base de datos",
                    "Tienen control total de la base de datos: " + propietarios + "."));
        }

        long deshabilitados = logins.stream().filter(l -> "DESHABILITADO".equals(l.estado())).count();
        if (deshabilitados > 0) {
            hallazgos.add(new HallazgoAuditoriaDTO(
                    "INFO",
                    "Logins deshabilitados",
                    deshabilitados + " login(s) están deshabilitados. Si ya no se usan, conviene eliminarlos."));
        }

        if (objetos.isEmpty()) {
            hallazgos.add(new HallazgoAuditoriaDTO(
                    "OK",
                    "Sin objetos con problemas",
                    "No se detectaron referencias rotas, índices deshabilitados, restricciones no confiables ni triggers deshabilitados."));
        } else {
            hallazgos.add(new HallazgoAuditoriaDTO(
                    "ADVERTENCIA",
                    "Objetos con problemas: " + objetos.size(),
                    "Revise la sección de objetos inválidos. Desde el módulo de Mantenimiento se pueden recompilar o reconstruir."));
        }

        return hallazgos;
    }

    private static String miembrosDe(List<AsignacionRolDTO> roles, String ambito, String rol) {
        return roles.stream()
                .filter(r -> ambito.equals(r.ambito()) && rol.equalsIgnoreCase(r.rol()))
                .map(AsignacionRolDTO::miembro)
                .collect(Collectors.joining(", "));
    }
}