package services;

import IRepositories.ICarRepository;
import IRepositories.IDTPRepository;
import IRepositories.IParticipantRepository;
import entities.Car;
import entities.DCP;
import entities.DTP;
import entities.Participant;
import exceptions.RepositoryException;
import logger.CustomLogger;
import params.CarParams;
import params.DTPParams;
import params.Params;
import params.ParticipantParams;
import permission.Permission;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DTPService {
    private final IDTPRepository dtpRepository;
    private final ICarRepository carRepository;
    private final IParticipantRepository prRepository;
    private final UserService usrService;

    public DTPService(IDTPRepository dtpRepository, ICarRepository carRepository, IParticipantRepository prRepository, UserService usrService) {
        this.dtpRepository = dtpRepository;
        this.carRepository = carRepository;
        this.prRepository = prRepository;
        this.usrService = usrService;
    }

    public List<DTP> getDTPsByParams(int usrid, DTPParams params) throws AccessDeniedException, RepositoryException {
//        if (!usrService.hasPermission(usrid, Permission.GET_DTP_BY_PARAMS)) {
//            CustomLogger.logWarning("User do not have main.java.permission", this.getClass().getSimpleName());
//            throw new AccessDeniedException("You do not have main.java.permission to perform this operation.");
//        }
//        else
//            CustomLogger.logInfo("Successful permissions", this.getClass().getSimpleName());
        List<DTP> dtps = dtpRepository.getDTPByParams(params);
        CustomLogger.logInfo("Succesful of getting dtps", this.getClass().getSimpleName());
        return dtps;
    }
    public List<Car> getCarsByParams(int usrid, CarParams params) throws AccessDeniedException, RepositoryException {
        if (!usrService.hasPermission(usrid, Permission.GET_CAR_BY_PARAMS)) {
            CustomLogger.logWarning("User do not have main.java.permission", this.getClass().getSimpleName());
            throw new AccessDeniedException("You do not have main.java.permission to perform this operation.");
        }
        else
            CustomLogger.logInfo("Successful permissions", this.getClass().getSimpleName());
        List<Car> cars =  carRepository.getCarsByParams(params);
        CustomLogger.logInfo("Successful of getting cars", this.getClass().getSimpleName());
        return cars;
    }
    public List<Participant> getParticsByParams(int usrid, ParticipantParams params) throws AccessDeniedException, RepositoryException {
        if (!usrService.hasPermission(usrid, Permission.GET_PARTICIPANT_BY_PARAMS)){
            CustomLogger.logInfo("User access main.java.permission", this.getClass().getSimpleName());
            throw new AccessDeniedException("You do not have main.java.permission to perform this operation.");
        }
        else
            CustomLogger.logInfo("Successful permissions", this.getClass().getSimpleName());
        List<Participant> prs = prRepository.getParticByParams(params);
        CustomLogger.logInfo("Successful of getting participants", this.getClass().getSimpleName());
        return prs;
    }

    public DTP getDTP(int usrid, int dtpid) throws AccessDeniedException, RepositoryException {
        if (!usrService.hasPermission(usrid, Permission.GET_DTP)){
            CustomLogger.logInfo("User access main.java.permission", this.getClass().getSimpleName());
            throw new AccessDeniedException("You do not have main.java.permission to perform this operation.");
        }
        else
            CustomLogger.logInfo("Successful permissions", this.getClass().getSimpleName());
        DTP dtp = dtpRepository.getDTP(dtpid);
        CustomLogger.logInfo("Successful of getting dtp", this.getClass().getSimpleName());
        return dtp;
    }
    public Car getCar(int usrid, int carid) throws AccessDeniedException, RepositoryException {
        if (!usrService.hasPermission(usrid, Permission.GET_CAR)){
            CustomLogger.logInfo("User access main.java.permission", this.getClass().getSimpleName());
            throw new AccessDeniedException("You do not have main.java.permission to perform this operation.");
        }
        else
            CustomLogger.logInfo("Successful permissions", this.getClass().getSimpleName());
        Car car = carRepository.getCar(carid);
        CustomLogger.logInfo("Successful of getting car", this.getClass().getSimpleName());
        return car;
    }
    public Participant getParticipant(int usrid, int prtid) throws AccessDeniedException, RepositoryException {
        if (!usrService.hasPermission(usrid, Permission.GET_PARTICIPANT)){
            CustomLogger.logInfo("User access main.java.permission", this.getClass().getSimpleName());
            throw new AccessDeniedException("You do not have main.java.permission to perform this operation.");
        }
        else
            CustomLogger.logInfo("Successful permissions", this.getClass().getSimpleName());
        Participant pr = prRepository.getParticipant(prtid);
        CustomLogger.logInfo("Successful of getting participant", this.getClass().getSimpleName());
        return pr;
    }

    public DCP getAllByParams(int usrid, Params params) throws AccessDeniedException, RepositoryException {
        if (!usrService.hasPermission(usrid, Permission.GET_ALL_BY_PARAMS)){
            CustomLogger.logInfo("User access main.java.permission", this.getClass().getSimpleName());
            throw new AccessDeniedException("You do not have main.java.permission to perform this operation.");
        }
        else
            CustomLogger.logInfo("Successful permissions", this.getClass().getSimpleName());
        List<DTP> dtps = new ArrayList<>();
        List<Car> crs = new ArrayList<>();
        List<Participant> prs = new ArrayList<>();

        if (params.exportDTP) {
            dtps = dtpRepository.getDTPByParams(params.dtpps);

            if (params.exportCar) {
                // Получаем id дтп
                List<Integer> dtpIds = dtps.stream().map(DTP::getId).collect(Collectors.toList());
                params.carps.dtp_ids = dtpIds;
                crs = carRepository.getCarsByParams(params.carps);

                if (params.exportParticipant) {
                    // Получаем id машин
                    List<Integer> carIds = crs.stream().map(Car::getId).collect(Collectors.toList());
                    params.pcps.car_ids = carIds;
                    prs = prRepository.getParticByParams(params.pcps);
                }
            }
        } else if (params.exportCar) {
            // Получаем id дтп
            crs = carRepository.getCarsByParams(params.carps);

            if (params.exportParticipant) {
                // Получаем id машин
                List<Integer> carIds = crs.stream().map(Car::getId).collect(Collectors.toList());
                params.pcps.car_ids = carIds;
                prs = prRepository.getParticByParams(params.pcps);
            }
        } else if (params.exportParticipant) {
            prs = prRepository.getParticByParams(params.pcps);
        }

        CustomLogger.logInfo("Successful of getting DCP", this.getClass().getSimpleName());
        return new DCP(dtps, crs, prs);
    }

    public boolean addDTP(int usrid, DTP dtp) throws AccessDeniedException, RepositoryException {
        if (!usrService.hasPermission(usrid, Permission.ADD_DTP)) {
            CustomLogger.logInfo("User access main.java.permission", this.getClass().getSimpleName());
            throw new AccessDeniedException("You do not have main.java.permission to perform this operation.");
        }
        else
            CustomLogger.logInfo("Successful permissions", this.getClass().getSimpleName());
        boolean result = dtpRepository.saveDTP(dtp);
        CustomLogger.logInfo("Successful saving", this.getClass().getSimpleName());
        return result;
    }
    public boolean editDTP(int usrid, DTP dtp) throws AccessDeniedException, RepositoryException {
        if (!usrService.hasPermission(usrid, Permission.EDIT_DTP)) {
            CustomLogger.logInfo("User access main.java.permission", this.getClass().getSimpleName());
            throw new AccessDeniedException("You do not have main.java.permission to perform this operation.");
        }
        else
            CustomLogger.logInfo("Successful permissions", this.getClass().getSimpleName());
        boolean result = dtpRepository.editDTP(dtp);
        CustomLogger.logInfo("Successful editing", this.getClass().getSimpleName());
        return result;
    }
    public boolean deleteDTP(int usrid, int dtpId) throws AccessDeniedException, RepositoryException {
        if (!usrService.hasPermission(usrid, Permission.DELETE_DTP)) {
            CustomLogger.logInfo("User access main.java.permission", this.getClass().getSimpleName());
            throw new AccessDeniedException("You do not have main.java.permission to perform this operation.");
        }
        else
            CustomLogger.logInfo("Successful permissions", this.getClass().getSimpleName());
        CarParams carParams = new CarParams();
        carParams.dtp_ids = Collections.singletonList(dtpId);
        ParticipantParams prParams = new ParticipantParams();
//        List<Car> cars = carRepository.getCarsByParams(carParams);
//        for (int i = 0; i < cars.size(); i++)
//        {
//            prParams.car_ids = Collections.singletonList(cars.get(i).getId());
//            List<Participant> prts = prRepository.getParticByParams(prParams);
//            for (int j = 0; j < prts.size(); j++)
//                prRepository.delParticipant(prts.get(j).getId());
//            carRepository.delCar(cars.get(i).getId());
//        }
        return dtpRepository.delDTP(dtpId);
    }

    public boolean addCar(int usrid, Car cr) throws AccessDeniedException, RepositoryException {
        if (!usrService.hasPermission(usrid, Permission.ADD_CAR)){
            CustomLogger.logInfo("User access main.java.permission", this.getClass().getSimpleName());
            throw new AccessDeniedException("You do not have main.java.permission to perform this operation.");
        }
        else
            CustomLogger.logInfo("Successful permissions", this.getClass().getSimpleName());
        boolean result = carRepository.saveCar(cr);
        CustomLogger.logInfo("Method saveCar() has been executed", this.getClass().getSimpleName());
        return result;
    }
    public boolean editCar(int usrid, Car cr) throws AccessDeniedException, RepositoryException {
        if (!usrService.hasPermission(usrid, Permission.EDIT_CAR)) {
            CustomLogger.logInfo("User access main.java.permission", this.getClass().getSimpleName());
            throw new AccessDeniedException("You do not have main.java.permission to perform this operation.");
        }
        else
            CustomLogger.logInfo("Successful permissions", this.getClass().getSimpleName());
        boolean result = carRepository.editCar(cr);
        CustomLogger.logInfo("Method editCar() has been executed", this.getClass().getSimpleName());
        return result;
    }
    public boolean deleteCar(int usrid, int del_id) throws AccessDeniedException, RepositoryException {
        if (!usrService.hasPermission(usrid, Permission.DELETE_CAR)) {
            CustomLogger.logInfo("User access main.java.permission", this.getClass().getSimpleName());
            throw new AccessDeniedException("You do not have main.java.permission to perform this operation.");
        }
        else
            CustomLogger.logInfo("Successful permissions", this.getClass().getSimpleName());
        boolean result = carRepository.delCar(del_id);
        CustomLogger.logInfo("Method delCar() has been executed", this.getClass().getSimpleName());
        return result;
    }

    public boolean addParticipant(int usrid, Participant pr) throws AccessDeniedException, RepositoryException {
        if (!usrService.hasPermission(usrid, Permission.ADD_PARTICIPANT)) {
            CustomLogger.logInfo("User access main.java.permission", this.getClass().getSimpleName());
            throw new AccessDeniedException("You do not have main.java.permission to perform this operation.");
        }
        else
            CustomLogger.logInfo("Successful permissions", this.getClass().getSimpleName());
        boolean result = prRepository.saveParticipant(pr);
        CustomLogger.logInfo("Method saveParticipant() has been executed", this.getClass().getSimpleName());
        return result;
    }
    public boolean editParticipant(int usrid, Participant pr) throws AccessDeniedException, RepositoryException {
        if (!usrService.hasPermission(usrid, Permission.EDIT_PARTICIPANT)) {
            CustomLogger.logInfo("User access main.java.permission", this.getClass().getSimpleName());
            throw new AccessDeniedException("You do not have main.java.permission to perform this operation.");
        }
        else
            CustomLogger.logInfo("Successful permissions", this.getClass().getSimpleName());
        boolean result = prRepository.editParticipant(pr);
        CustomLogger.logInfo("Method editParticipant() has been executed", this.getClass().getSimpleName());
        return result;
    }
    public boolean deleteParticipant(int usrid, int id) throws AccessDeniedException, RepositoryException {
        if (!usrService.hasPermission(usrid, Permission.DELETE_PARTICIPANT)) {
            CustomLogger.logInfo("User access main.java.permission", this.getClass().getSimpleName());
            throw new AccessDeniedException("You do not have main.java.permission to perform this operation.");
        }
        else
            CustomLogger.logInfo("Successful permissions", this.getClass().getSimpleName());
        boolean result = prRepository.delParticipant(id);
        CustomLogger.logInfo("Method delParticipant() has been executed", this.getClass().getSimpleName());
        return result;
    }
}
