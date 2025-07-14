// ==================== Implementación del Builder ====================
package app.simplestudio.com.service.builder.impl;

import app.simplestudio.com.dto.*;
import app.simplestudio.com.mh.FuncionesService;
import app.simplestudio.com.models.entity.*;
import app.simplestudio.com.service.builder.FacturaBuilder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class FacturaBuilderImpl implements FacturaBuilder {

    private static final Logger log = LoggerFactory.getLogger(FacturaBuilderImpl.class);

    private final FuncionesService funcionesService;

    public FacturaBuilderImpl(FuncionesService funcionesService) {
        this.funcionesService = funcionesService;
    }

    @Override
    public Factura construir(FacturaRequestDTO request, Emisor emisor, String clave, String consecutivo) {
        log.debug("Construyendo factura para clave: {}", clave);

        return new FacturaBuilderInterno(request, emisor, clave, consecutivo)
            .construirDatosBasicos()
            .construirEmisor()
            .construirReceptor()
            .construirCondicionesVenta()
            .construirItems()
            .construirReferencias()
            .construirOtrosCargos()
            .construirTotales()
            .build();
    }

    /**
     * Builder interno para construcción fluida
     */
    private class FacturaBuilderInterno {

        private final FacturaRequestDTO request;
        private final Emisor emisor;
        private final String clave;
        private final String consecutivo;
        private final Factura factura;

        public FacturaBuilderInterno(FacturaRequestDTO request, Emisor emisor,
            String clave, String consecutivo) {
            this.request = request;
            this.emisor = emisor;
            this.clave = clave;
            this.consecutivo = consecutivo;
            this.factura = new Factura();
        }

        public FacturaBuilderInterno construirDatosBasicos() {
            factura.setTipoDocumento(request.tipoDocumento());
            factura.setClave(clave);
            factura.setConsecutivo(consecutivo);
            factura.setCodigoActividad(request.codigoActividadEmisor());
            factura.setFechaEmision(obtenerFechaEmision());
            factura.setIdentificacion(request.emisor());
            factura.setSituacion(request.situacion());
            factura.setTerminal(funcionesService.strPad(request.terminal().toString(), 5, "0", "STR_PAD_LEFT"));
            factura.setSucursal(funcionesService.strPad(request.sucursal().toString(), 3, "0", "STR_PAD_LEFT"));
            factura.setNumeroFactura(request.numeroFactura());

            return this;
        }

        public FacturaBuilderInterno construirEmisor() {
            // Para FEC (Factura Electrónica Compra) el emisor y receptor se intercambian
            if ("FEC".equals(request.tipoDocumento())) {
                construirEmisorParaFEC();
            } else {
                construirEmisorNormal();
            }

            return this;
        }

        private void construirEmisorNormal() {
            factura.setEmisorNombre(emisor.getNombreRazonSocial());
            factura.setEmisorTipoIdentif("0" + emisor.getTipoDeIdentificacion().getId().toString());
            factura.setEmisorNumIdentif(request.emisor());
            factura.setNombreComercial(emisor.getNombreComercial());

            // Ubicación del emisor
            if (emisor.getProvincia() != null) {
                factura.setEmisorProv(emisor.getProvincia().getId().toString());
            }
            if (emisor.getCanton() != null) {
                factura.setEmisorCanton(funcionesService.strPad(
                    emisor.getCanton().getNumeroCanton(), 2, "0", "STR_PAD_LEFT"));
            }
            if (emisor.getDistrito() != null) {
                factura.setEmisorDistrito(funcionesService.strPad(
                    emisor.getDistrito().getNumeroDistrito(), 2, "0", "STR_PAD_LEFT"));
            }
            if (emisor.getBarrio() != null) {
                factura.setEmisorBarrio(funcionesService.strPad(
                    emisor.getBarrio().getNumeroBarrio(), 2, "0", "STR_PAD_LEFT"));
            }

            factura.setEmisorOtrasSenas(emisor.getOtrasSenas());
            factura.setEmisorCodPaisTel(emisor.getCodigoPais());
            factura.setEmisorTel(emisor.getTelefono());
            factura.setEmisorCodPaisFax(emisor.getCodigoPais());
            factura.setEmisorFax(emisor.getFax());
            factura.setEmisorEmail(emisor.getEmail());
        }

        private void construirEmisorParaFEC() {
            // En FEC, los datos del receptor del request van como emisor
            factura.setEmisorNombre(request.receptorNombre());
            factura.setEmisorTipoIdentif(request.receptorTipoIdentif());
            factura.setEmisorNumIdentif(request.receptorNumIdentif());

            factura.setEmisorProv(request.receptorProvincia());
            factura.setEmisorCanton(request.receptorCanton());
            factura.setEmisorDistrito(request.receptorDistrito());
            factura.setEmisorBarrio(request.receptorBarrio());
            factura.setEmisorOtrasSenas(request.receptorOtrasSenas());
            factura.setEmisorCodPaisTel(request.receptorCodPaisTel());
            factura.setEmisorTel(request.receptorTel());
            factura.setEmisorCodPaisFax(request.receptorCodPaisFax());
            factura.setEmisorFax(request.receptorFax());
            factura.setEmisorEmail(request.receptorEmail());
        }

        public FacturaBuilderInterno construirReceptor() {
            factura.setOmitirReceptor(request.omitirReceptor());

            if ("FEC".equals(request.tipoDocumento())) {
                construirReceptorParaFEC();
            } else {
                construirReceptorNormal();
            }

            return this;
        }

        private void construirReceptorNormal() {
            factura.setReceptorNombre(request.receptorNombre());
            factura.setReceptorTipoIdentif(request.receptorTipoIdentif());
            factura.setReceptor_num_identif(request.receptorNumIdentif());

            factura.setReceptorProvincia(request.receptorProvincia());
            factura.setReceptorCanton(request.receptorCanton());
            factura.setReceptorDistrito(request.receptorDistrito());
            factura.setReceptorBarrio(request.receptorBarrio());
            factura.setReceptorOtrasSenas(request.receptorOtrasSenas());

            factura.setReceptorCodPaisTel(request.receptorCodPaisTel());
            factura.setReceptorTel(request.receptorTel());
            factura.setReceptorCodPaisFax(request.receptorCodPaisFax());
            factura.setReceptorFax(request.receptorFax());
            factura.setReceptorEmail(request.receptorEmail());
        }

        private void construirReceptorParaFEC() {
            // En FEC, los datos del emisor van como receptor
            factura.setReceptorNombre(emisor.getNombreRazonSocial());
            factura.setReceptorTipoIdentif("0" + emisor.getTipoDeIdentificacion().getId().toString());
            factura.setReceptor_num_identif(request.emisor());

            if (emisor.getProvincia() != null && emisor.getCanton() != null &&
                emisor.getDistrito() != null && emisor.getOtrasSenas() != null) {

                factura.setReceptorProvincia(emisor.getProvincia().getId().toString());
                factura.setReceptorCanton(funcionesService.strPad(
                    emisor.getCanton().getNumeroCanton(), 2, "0", "STR_PAD_LEFT"));
                factura.setReceptorDistrito(funcionesService.strPad(
                    emisor.getDistrito().getNumeroDistrito(), 2, "0", "STR_PAD_LEFT"));

                if (emisor.getBarrio() != null) {
                    factura.setReceptorBarrio(funcionesService.strPad(
                        emisor.getBarrio().getNumeroBarrio(), 2, "0", "STR_PAD_LEFT"));
                }

                factura.setReceptorOtrasSenas(emisor.getOtrasSenas());
            }

            factura.setReceptorCodPaisTel(emisor.getCodigoPais());
            factura.setReceptorTel(emisor.getTelefono());
            factura.setReceptorCodPaisFax(emisor.getCodigoPais());
            factura.setReceptorFax(emisor.getFax());
            factura.setReceptorEmail(emisor.getEmail());
        }

        public FacturaBuilderInterno construirCondicionesVenta() {
            factura.setCondVenta(request.condVenta());
            factura.setPlazoCredito(request.plazoCredito().toString());
            factura.setMedioPago(request.medioPago());
            factura.setMedioPago2(request.medioPago2());
            factura.setMedioPago3(request.medioPago3());
            factura.setMedioPago4(request.medioPago4());

            // Moneda y tipo de cambio
            String codMoneda = request.codMoneda() != null ? request.codMoneda() : "CRC";
            String tipoCambio = request.tipoCambio() != null ? request.tipoCambio().toString() : "1.00";

            factura.setCodMoneda(codMoneda);
            factura.setTipoCambio(tipoCambio);

            return this;
        }

        public FacturaBuilderInterno construirItems() {
            if (request.detalleLinea() != null && !request.detalleLinea().isEmpty()) {
                List<ItemFactura> items = new ArrayList<>();

                for (DetalleLineaDTO detalle : request.detalleLinea()) {
                    ItemFactura item = construirItemFactura(detalle);
                    items.add(item);
                }

                factura.setItems(items);
            }

            return this;
        }

        private ItemFactura construirItemFactura(DetalleLineaDTO detalle) {
            ItemFactura item = new ItemFactura();

            // Datos básicos del item
            item.setNumeroLinea(detalle.numeroLinea());
            item.setPartidaArancelaria(detalle.partidaArancelaria());
            item.setCodigo(detalle.codigo());
            item.setCantidad(detalle.cantidad().doubleValue());
            item.setUnidadMedida(detalle.unidadMedida());
            item.setUnidadMedidaComercial(detalle.unidadMedidaComercial());
            item.setDetalle(detalle.detalle());
            item.setPrecioUnitario(detalle.precioUnitario().doubleValue());
            item.setMontoTotal(detalle.montoTotal().doubleValue());
            item.setSubTotal(detalle.subTotal().doubleValue());
            item.setImpuestoNeto(detalle.impuestoNeto() != null ? detalle.impuestoNeto().doubleValue() : 0.0);
            item.setMontoTotalLinea(detalle.montoTotalLinea().doubleValue());

            // Códigos comerciales
            construirCodigosComerciales(item, detalle.codigoComercial());

            // Descuentos
            construirDescuentos(item, detalle.descuentos());

            // Impuestos
            construirImpuestosItem(item, detalle.impuestos());

            return item;
        }

        private void construirCodigosComerciales(ItemFactura item, List<CodigoComercialDTO> codigos) {
            if (codigos != null && !codigos.isEmpty()) {
                for (int i = 0; i < Math.min(codigos.size(), 5); i++) {
                    CodigoComercialDTO codigo = codigos.get(i);
                    switch (i) {
                        case 0:
                            item.setCodigoComercialTipo(codigo.tipo());
                            item.setCodigoComercialCodigo(codigo.codigo());
                            break;
                        case 1:
                            item.setCodigoComercialTipo2(codigo.tipo());
                            item.setCodigoComercialCodigo2(codigo.codigo());
                            break;
                        case 2:
                            item.setCodigoComercialTipo3(codigo.tipo());
                            item.setCodigoComercialCodigo3(codigo.codigo());
                            break;
                        case 3:
                            item.setCodigoComercialTipo4(codigo.tipo());
                            item.setCodigoComercialCodigo4(codigo.codigo());
                            break;
                        case 4:
                            item.setCodigoComercialTipo5(codigo.tipo());
                            item.setCodigoComercialCodigo5(codigo.codigo());
                            break;
                    }
                }
            }
        }

        private void construirDescuentos(ItemFactura item, List<DescuentoDTO> descuentos) {
            if (descuentos != null && !descuentos.isEmpty()) {
                for (int i = 0; i < Math.min(descuentos.size(), 5); i++) {
                    DescuentoDTO descuento = descuentos.get(i);
                    switch (i) {
                        case 0:
                            item.setMontoDescuento(descuento.montoDescuento().doubleValue());
                            item.setNaturalezaDescuento(descuento.naturalezaDescuento());
                            break;
                        case 1:
                            item.setMontoDescuento2(descuento.montoDescuento().doubleValue());
                            item.setNaturalezaDescuento2(descuento.naturalezaDescuento());
                            break;
                        case 2:
                            item.setMontoDescuento3(descuento.montoDescuento().doubleValue());
                            item.setNaturalezaDescuento3(descuento.naturalezaDescuento());
                            break;
                        case 3:
                            item.setMontoDescuento4(descuento.montoDescuento().doubleValue());
                            item.setNaturalezaDescuento4(descuento.naturalezaDescuento());
                            break;
                        case 4:
                            item.setMontoDescuento5(descuento.montoDescuento().doubleValue());
                            item.setNaturalezaDescuento5(descuento.naturalezaDescuento());
                            break;
                    }
                }
            }
        }

        private void construirImpuestosItem(ItemFactura item, List<ImpuestoItemDTO> impuestos) {
            if (impuestos != null && !impuestos.isEmpty()) {
                List<ImpuestosItemFactura> impuestosItem = new ArrayList<>();

                for (ImpuestoItemDTO ImpuestoItemDTO : impuestos) {
                    ImpuestosItemFactura impuesto = new ImpuestosItemFactura();
                    impuesto.setCodigo(ImpuestoItemDTO.codigo());
                    impuesto.setCodigoTarifa(ImpuestoItemDTO.codigoTarifa());
                    impuesto.setTarifa(ImpuestoItemDTO.tarifa().doubleValue());
                    impuesto.setMonto(ImpuestoItemDTO.monto().doubleValue());

                    if (ImpuestoItemDTO.impuestoNeto() != null) {
                        impuesto.setImpuestoNeto(ImpuestoItemDTO.impuestoNeto().doubleValue());
                    }

                    // Exoneración si existe
                    if (ImpuestoItemDTO.exoneracion() != null) {
                        ExoneracionImpuestoItemFactura exoneracion = construirExoneracion(ImpuestoItemDTO.exoneracion());
                        impuesto.addItemFacturaImpuestosExoneracion(exoneracion);
                    }

                    impuestosItem.add(impuesto);
                }

                item.setImpuestosItemFactura(impuestosItem);
            }
        }

        private ExoneracionImpuestoItemFactura construirExoneracion(ExoneracionDTO exoneracionDTO) {
            ExoneracionImpuestoItemFactura exoneracion = new ExoneracionImpuestoItemFactura();
            exoneracion.setTipoDocumento(exoneracionDTO.tipoDocumento());
            exoneracion.setNumeroDocumento(exoneracionDTO.numeroDocumento());
            exoneracion.setNombreInstitucion(exoneracionDTO.nombreInstitucion());
            exoneracion.setFechaEmision(exoneracionDTO.fechaEmision());
            exoneracion.setMontoExoneracion(exoneracionDTO.montoExoneracion().doubleValue());
            exoneracion.setPorcentajeExoneracion(exoneracionDTO.porcentajeExoneracion());
            return exoneracion;
        }

        public FacturaBuilderInterno construirReferencias() {
            if (request.referencias() != null && !request.referencias().isEmpty()) {

                for (ReferenciaDTO referenciaDTO : request.referencias()) {
                    FacturaReferencia referencia = new FacturaReferencia();

                    // Extraer tipo de documento de la clave (posiciones 29-31)
                    String numero = referenciaDTO.numero();
                    if (numero != null && numero.length() == 50) {
                        referencia.setTipoDoc(numero.substring(29, 31));
                    }

                    referencia.setNumero(numero);
                    referencia.setFechaEmision(referenciaDTO.fechaEmision());
                    referencia.setCodigo(referenciaDTO.codigo());
                    referencia.setRazon(referenciaDTO.razon());

                    factura.addReferenciaFactura(referencia);

                }

            }

            return this;
        }

        public FacturaBuilderInterno construirOtrosCargos() {
            if (request.otrosCargos() != null && !request.otrosCargos().isEmpty()) {
                // Limitar a máximo 15 cargos como en el código original
                int maxCargos = Math.min(request.otrosCargos().size(), 15);

                for (int i = 0; i < maxCargos; i++) {
                    OtroCargoDTO cargoDTO = request.otrosCargos().get(i);
                    FacturaOtrosCargos cargo = new FacturaOtrosCargos();

                    cargo.setTipoDocumento(cargoDTO.tipoDocumento());
                    cargo.setNumeroIdentidadTercero(cargoDTO.numeroIdentidadTercero());
                    cargo.setNombreTercero(cargoDTO.nombreTercero());
                    cargo.setDetalle(cargoDTO.detalle());
                    cargo.setPorcentaje(cargoDTO.porcentaje());
                    cargo.setMontoCargo(cargoDTO.montoCargo().toString());

                    factura.addOtrosCargos(cargo);

                }

            }

            return this;
        }

        public FacturaBuilderInterno construirTotales() {
            factura.setTotalServGravados(request.totalServGravados().toString());
            factura.setTotalServExentos(request.totalServExentos().toString());
            factura.setTotalServExonerado(request.totalServExonerado().toString());
            factura.setTotalMercGravadas(request.totalMercGravadas().toString());
            factura.setTotalMercExentas(request.totalMercExentas().toString());
            factura.setTotalMercExonerada(request.totalMercExonerada().toString());
            factura.setTotalGravados(request.totalGravados().toString());
            factura.setTotalExentos(request.totalExentos().toString());
            factura.setTotalExonerado(request.totalExonerado().toString());
            factura.setTotalVentas(request.totalVentas().toString());
            factura.setTotalDescuentos(request.totalDescuentos().toString());
            factura.setTotalVentaNeta(request.totalVentasNeta().toString());
            factura.setTotalImp(request.totalImp().toString());
            factura.setTotalIVADevuelto(request.totalIVADevuelto().toString());
            factura.setTotalOtrosCargos(request.totalOtrosCargos().toString());
            factura.setTotalComprobante(request.totalComprobante().toString());
            factura.setOtros(request.otros());

            return this;
        }

        public Factura build() {
            log.debug("Factura construida exitosamente. Clave: {}", factura.getClave());
            return factura;
        }

        private String obtenerFechaEmision() {
            // Si viene fecha personalizada, usar esa; si no, generar nueva
            if (request.fechaEmision() != null && !request.fechaEmision().isEmpty()) {
                return request.fechaEmision();
            }

            // Generar fecha actual en formato requerido
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            return now.format(formatter) + "-06:00";
        }
    }
}