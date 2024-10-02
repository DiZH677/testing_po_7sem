package params;

import java.util.Date;


public class DTPParams {
    public Date dtpBegin;
    public Date dtpEnd;
    public Integer dtpIdBegin;
    public Integer dtpIdEnd;
    public Integer countTs;

    public DTPParams() {
        // Все параметры будут инициализированы значением по умолчанию (null)
    }

    // Дополнительный конструктор, позволяющий инициализировать параметры явно
    public DTPParams(Integer dtpIdBegin, Integer dtpIdEnd, Date dtpBegin, Date dtpEnd, Integer countTs) {
        this.dtpBegin = dtpBegin;
        this.dtpEnd = dtpEnd;
        this.dtpIdBegin = dtpIdBegin;
        this.dtpIdEnd = dtpIdEnd;
        this.countTs = countTs;
    }
}

