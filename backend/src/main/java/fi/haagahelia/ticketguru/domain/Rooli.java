package fi.haagahelia.ticketguru.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Rooli {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nimi;

    @OneToMany(mappedBy = "rooli")
    private List<Kayttaja> kayttajat = new ArrayList<>();

    public Rooli() {
    }

    public Rooli(String nimi) {
        this.nimi = nimi;
    }

    public Long getId() {
        return id;
    }

    public String getNimi() {
        return nimi;
    }

    public void setNimi(String nimi) {
        this.nimi = nimi;
    }

    public List<Kayttaja> getKayttajat() {
        return kayttajat;
    }

    public void setKayttajat(List<Kayttaja> kayttajat) {
        this.kayttajat = kayttajat;
    }
}
