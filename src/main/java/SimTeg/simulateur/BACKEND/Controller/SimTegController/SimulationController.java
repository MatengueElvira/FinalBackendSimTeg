package SimTeg.simulateur.BACKEND.Controller.SimTegController;

import SimTeg.simulateur.BACKEND.Service.SimTegService.ExportSimulationService;
import SimTeg.simulateur.BACKEND.Service.SimTegService.ImportationFichierExcel;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import SimTeg.simulateur.BACKEND.Dto.SimTegDto.SimulationDtoRequest;
import SimTeg.simulateur.BACKEND.Dto.SimTegDto.SimulationDtoResponse;
import SimTeg.simulateur.BACKEND.Dto.SimTegDto.SimulationRequestGlobal;
import SimTeg.simulateur.BACKEND.Entity.SimTegEntity.Simulation;
import SimTeg.simulateur.BACKEND.Service.SimTegService.SimulationService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("api/simulation")
public class SimulationController {
    @Autowired
    private SimulationService simulationService;
    @Autowired
    private ImportationFichierExcel importationFichierExcel;
    @Autowired
    private ExportSimulationService pdfExportService;
    @PostMapping("calculer")
    public SimulationDtoResponse calculerTeg(   @io.swagger.v3.oas.annotations.parameters.RequestBody @RequestBody SimulationDtoRequest simulationDtoRequest){
         return simulationService.doSimulation(simulationDtoRequest);
    }
    @PostMapping("save/{email}")
    public Simulation saveSimulation(@io.swagger.v3.oas.annotations.parameters.RequestBody @RequestBody SimulationRequestGlobal simulationRequestGlobal, @PathVariable  String email){
        return  simulationService.EnregistrerSimulation(simulationRequestGlobal,email);
    }
    @PutMapping("mettrejour/{idSimulation}")
    public  Simulation mettreAjour(@io.swagger.v3.oas.annotations.parameters.RequestBody @RequestBody SimulationDtoRequest simulationDtoRequest, @PathVariable Integer idSimulation){
         return  simulationService.UptdateSimulation(simulationDtoRequest,idSimulation);
    }
    @DeleteMapping("supprimer/{idSimulation}")
    public boolean supprimerSimulation( @PathVariable Integer idSimulation){
        return simulationService.deleteSimulation(idSimulation);
    }


    @GetMapping("simulationUser/{id}")
    public List<Simulation> listeSimulationUser(@PathVariable Integer id){
        return simulationService.simulationParUserID(id);
    }

    @PostMapping("/import-excel")
    public ResponseEntity<?> importSimulationExcel( @RequestParam("file") MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            SimulationDtoRequest dto = importationFichierExcel.LireSimulation(inputStream);

            return ResponseEntity.ok(dto);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de l'importation du fichier : " + e.getMessage());
        }
    }
    @GetMapping("simulationParCategorieId/{userId}")
    public List<Simulation> listeSimulationCategorie(@PathVariable Integer userId , @io.swagger.v3.oas.annotations.parameters.RequestBody @RequestBody Integer categorieId){
        return simulationService.getAllSimulationUserByCategorieId(categorieId,userId);
    }
    @GetMapping("/simulation/export-pdf")
    public void exportSimulationAsPdf(@io.swagger.v3.oas.annotations.parameters.RequestBody @RequestBody SimulationRequestGlobal simulation, HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=simulation.pdf");

        pdfExportService.exportToPdf(simulation, response.getOutputStream());
    }


}
