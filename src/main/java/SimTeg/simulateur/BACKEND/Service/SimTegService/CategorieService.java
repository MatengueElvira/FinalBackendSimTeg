package SimTeg.simulateur.BACKEND.Service.SimTegService;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import SimTeg.simulateur.BACKEND.Dto.SimTegDto.CategorieDto;
import SimTeg.simulateur.BACKEND.Entity.SimTegEntity.CategorieCredit;
import SimTeg.simulateur.BACKEND.Entity.SimTegEntity.Simulation;
import SimTeg.simulateur.BACKEND.Entity.SimTegEntity.TauxUsure;
import SimTeg.simulateur.BACKEND.Repository.SimTegRepository.CategorieCreditRepository;
import SimTeg.simulateur.BACKEND.Dto.DataType.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Service
public class CategorieService {
    @Autowired
    private CategorieCreditRepository categorieCreditRepository;
    public CategorieCredit CreateCategorieCredit(CategorieDto categorieDto){
        CategorieCredit categorieCredit= new CategorieCredit();
        categorieCredit.setNomCategorie(categorieDto.getNomCategorie());
        categorieCredit.setDescription(categorieDto.getDescription());
        categorieCredit.setAssurances(categorieDto.getAssurances());
        categorieCredit.setFraisObligatoire(categorieDto.getFraisObligatoire());


        return  categorieCreditRepository.save(categorieCredit);

    }
    public CategorieDto GetCategorieCredit(Integer id){
        if(categorieCreditRepository.existsById(id)){
            Optional<CategorieCredit> categorieCredit= categorieCreditRepository.findById(id);
            return categorieCredit.map(this::convertToDto).orElse(null);
        };
        CategorieDto categorieDto=new CategorieDto();

      return  categorieDto;


    }
    public  boolean deleteCategorieCredit(Integer id){

        if (categorieCreditRepository.existsById(id)) {

            categorieCreditRepository.deleteById(id);
            return true;
        }
        return false;

    }

    public List<CategorieCredit> listCategorie(){
        if (categorieCreditRepository.findAll().isEmpty()){
            throw  new RuntimeException("la liste de categorie est vide ");
        }

        return  categorieCreditRepository.findAll();
    }
    public CategorieCredit UpdateCategorieCredit(Integer id,CategorieDto categorieDto){
        Optional<CategorieCredit> categorieCreditOptional=categorieCreditRepository.findById(id);
        CategorieCredit categorieCredit=categorieCreditOptional.get();
        categorieCredit.setNomCategorie(categorieDto.getNomCategorie());
        categorieCredit.setAssurances(categorieDto.getAssurances());
        categorieCredit.setDescription(categorieDto.getDescription());
        categorieCredit.setFraisObligatoire(categorieDto.getFraisObligatoire());
        return categorieCreditRepository.save(categorieCredit);
    }
    public CategorieDto convertToDto(CategorieCredit categorieCredit){

        CategorieDto dto = new CategorieDto();
        dto.setId(categorieCredit.getId());
        dto.setNomCategorie(categorieCredit.getNomCategorie());
        dto.setDescription(categorieCredit.getDescription());
        dto.setFraisObligatoire(categorieCredit.getFraisObligatoire());
        dto.setAssurances(categorieCredit.getAssurances());

        return dto;
    }
    public FrAss getChampCategorie(Integer categorieId) {
        Optional<CategorieCredit> categorieTest = categorieCreditRepository.findById(categorieId);

        if (categorieTest.isPresent()) {
            CategorieCredit categorieCredit = categorieTest.get();

            List<String> frais = categorieCredit.getFraisObligatoire();
            List<String> assurances = categorieCredit.getAssurances();

            // On initialise des listes vides si null
            if (frais == null) {
                frais = new ArrayList<>();
            }

            if (assurances == null) {
                assurances = new ArrayList<>();
            }

            FrAss fraisAssurance = new FrAss();
            fraisAssurance.setFrais(frais);
            fraisAssurance.setAssurance(assurances);

            return fraisAssurance;

        } else {
            throw new EntityNotFoundException("Catégorie de crédit avec ID " + categorieId + " introuvable.");
        }
    }


}
