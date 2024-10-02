package services;

import configurator.Configurator;
import entities.DCP;
import exceptions.RepositoryException;
import logger.CustomLogger;
import params.Params;
import report.ReportGenerator;

import java.nio.file.AccessDeniedException;

public class ReportService {
    private DTPService dtpService;
    private ReportGenerator reportGenerator;
    private Integer report_max_count;

    public ReportService(DTPService dtpService,
                         ReportGenerator repGen) {
        this.dtpService = dtpService;
        this.reportGenerator = repGen;
        configureReport("app.properties");
    }

    public void setReportGenerator(ReportGenerator repGen)
    {
        this.reportGenerator = repGen;
    }

    public void setDTPService(DTPService dS) { this.dtpService = dS; }

    public boolean save(int usrid, String fname, String format, Params params) throws AccessDeniedException, RepositoryException {
        DCP data;
        data = dtpService.getAllByParams(usrid, params);
        if (data.getDTPs().size() > report_max_count) {
            data.setDTPs(data.getDTPs().subList(0, report_max_count)); // Устанавливаем обновленный список ДТП
        }
        boolean result = reportGenerator.generateReport(fname, format, data); // вызываем метод генерации отчета
        CustomLogger.logInfo("method generateReport() has been executed", this.getClass().getSimpleName());
        return result; // Возвращаем false в случае, если формат не поддерживается
    }

    public byte[] get(int usrid, String format, Params params) throws AccessDeniedException, RepositoryException {
        DCP data;
        data = dtpService.getAllByParams(usrid, params);
        if (data.getDTPs().size() > report_max_count) {
            data.setDTPs(data.getDTPs().subList(0, report_max_count)); // Устанавливаем обновленный список ДТП
        }
        byte[] result = reportGenerator.getReport(format, data); // вызываем метод генерации отчета
        CustomLogger.logInfo("method get() has been executed", this.getClass().getSimpleName());
        return result; // Возвращаем false в случае, если формат не поддерживается
    }

    private void configureReport(String configFileName) {
        String value = Configurator.getValue("bl.report_max_count");
        if (value != null)
            report_max_count = Integer.valueOf(value);
        else
            report_max_count = 1000;
    }
}
