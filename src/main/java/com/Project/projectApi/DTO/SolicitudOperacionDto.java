package com.Project.projectApi.DTO;

import com.Project.projectApi.Enum.CategoriaGasto;

public record SolicitudOperacionDto(
        Integer idMonedero,
        Integer idMonederoOrigen,
        Integer idMonederoDestino,
        Double monto,
        CategoriaGasto categoria,
        String descripcion
) {
}
