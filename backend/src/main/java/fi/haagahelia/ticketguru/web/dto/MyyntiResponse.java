package fi.haagahelia.ticketguru.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import fi.haagahelia.ticketguru.domain.Kayttaja;
import fi.haagahelia.ticketguru.domain.Myyntitapahtuma;

public record MyyntiResponse(
        Long id,
        LocalDateTime myyntiaika,
        BigDecimal summa,
        String tapahtumanNimi,
        String myyjanNimi,
        List<LippuResponse> liput) {

    public static MyyntiResponse from(Myyntitapahtuma myynti, List<LippuResponse> liput) {
        Kayttaja myyja = myynti.getMyyja();
        return new MyyntiResponse(
                myynti.getId(),
                myynti.getMyyntiaika(),
                myynti.getSumma(),
                myynti.getTapahtuma().getNimi(),
                myyja.getEtunimi() + " " + myyja.getSukunimi(),
                liput);
    }
}
