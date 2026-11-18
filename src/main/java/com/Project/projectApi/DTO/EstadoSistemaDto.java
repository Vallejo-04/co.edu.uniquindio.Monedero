package com.Project.projectApi.DTO;

public record EstadoSistemaDto(
        int clientes,
        int monederos,
        int transacciones,
        double dineroEnElSistema,
        int programadasPendientes,
        TransaccionProgramadaDto proximaProgramada,
        int alturaArbolPuntos,
        boolean arbolBalanceado,
        int capacidadTablaClientes,
        int cadenaMasLargaTabla,
        int notificacionesEntregadas
) {
}
