package com.dietiestates25backend.business.service;

import com.dietiestates25.dto.ImmobileDTO;
import com.dietiestates25backend.business.entity.Immobile;
import com.dietiestates25backend.data.repository.ImmobileRepository;
import com.dietiestates25backend.data.repository.UtenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImmobileService {

    @Autowired
    private ImmobileRepository immobileRepository;
    @Autowired
    private UtenteRepository utenteRepository;

    public Immobile saveImmobile(ImmobileDTO immobileDTO, String email) {
        Immobile immobile = new Immobile();
        immobile.setTitolo(immobileDTO.getTitolo());
        immobile.setTipo(immobileDTO.getTipologia());
        immobile.setIndirizzo(immobileDTO.getIndirizzo());
        immobile.setPrezzo(immobileDTO.getPrezzo());
        immobile.setDescrizione(immobileDTO.getDescrizione());
        immobile.setDimensione(immobileDTO.getDimensione());
        immobile.setNumero_stanze(immobileDTO.getNumero_camere());
        immobile.setNumero_bagni(immobileDTO.getNumero_bagni());
        immobile.setClasseEnergetica(immobileDTO.getClasseEnergetica());
        immobile.setPiano(immobileDTO.getPiano());
        immobile.setAscensore(immobileDTO.isAscensore());
        immobile.setPortineria(immobileDTO.isPortineria());
        immobile.setClimatizzazione(immobileDTO.isClimatizzazione());
        immobile.setImmaginiUrls(immobileDTO.getImmaginiUrls());
        immobile.setLatitudine(immobileDTO.getLatitudine());
        immobile.setLongitudine(immobileDTO.getLongitudine());
        immobile.setVicinoScuole(immobileDTO.isVicinoScuole());
        immobile.setVicinoParchi(immobileDTO.isVicinoParchi());
        immobile.setVicinoTrasportoPubblico(immobileDTO.isVicinoTrasportoPubblico());
        //immobile.setAgente(utenteRepository.findByEmail(email).get());

        return immobileRepository.save(immobile);
    }
}