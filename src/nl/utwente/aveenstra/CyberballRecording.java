package nl.utwente.aveenstra;

import javafx.util.converter.DateTimeStringConverter;
import jxl.write.*;
import jxl.write.Number;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by Johan on 18-7-2015.
 */
public class CyberballRecording implements Observer {

    // public static int MULTIPLIER = 1000;
    private long startTime;
    private WritableWorkbook workbook;
    private WritableSheet sheet;
    private int row = 4;

    public CyberballRecording() throws FileNotFoundException, FileAlreadyExistsException, IOException {
        JoystickWrapper.getInstance().addObserver(this);
        File file = buildPath();
        if (file.exists()) {
            throw new FileAlreadyExistsException(file.getPath());
        }
        workbook = jxl.Workbook.createWorkbook(file);
        sheet = workbook.createSheet('R' + Main.getrNumber(), 0);
        try {
            sheet.addCell(new Label(0, 0, "Rnummer"));
            sheet.addCell(new Label(0, 1, 'R' + Main.getrNumber()));
            sheet.addCell(new Label(1, 0, "Author"));
            sheet.addCell(new Label(1, 1, Main.PREFERENCES.get(Main.AUTHOR, null)));
            sheet.addCell(new Label(2, 0, "scoring date"));
            sheet.addCell(new DateTime(2,1,new Date()));
            sheet.addCell(new Label(3, 0, "Child_Press_Buttons_from_the_beginning"));
            sheet.addCell(new Label(4, 0, "Pressing_buttons_until_end"));
            sheet.addCell(new Label(5, 0, "Film_date"));
            sheet.addCell(new Label(5, 1, Main.getFilmDate()));
            sheet.addCell(new Label(6, 0, "Tic"));


            sheet.addCell(new Label(0, 3, "Time"));
            for (int i = 1; i < ComponentWrapper.componentWrappers.length; i++) {
                sheet.addCell(new Label(i, 3, ComponentWrapper.componentWrappers[i].getName()));
            }
        } catch (WriteException e) {
            e.printStackTrace();
        }
        startTime = System.currentTimeMillis();
    }

    public static File buildPath() {
        return buildPath(new File(Main.PREFERENCES.get(Main.OUTPUT_DIR, null)), Main.getrNumber());
    }

    public static File buildPath(File folder, String rNumber) {
        return folder.toPath().resolve('R' + rNumber + ".xls").toFile();
    }

    public static void main(String[] args) {
        System.out.println(new DateTimeStringConverter().toString(new Date()));
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg == null) {
            try {
                sheet.addCell(new Number(0, row, (System.currentTimeMillis() - startTime) / 1000.0));
                for (int i = 1; i < ComponentWrapper.componentWrappers.length; i++) {
                    float average = ComponentWrapper.componentWrappers[i].getAverage();
                    if (ComponentWrapper.componentWrappers[i].isButton()) {
                        sheet.addCell(new Number(i, row, Math.ceil(average)));
                    } else {
                        sheet.addCell(new Number(i, row, Math.max(0, average)));
                    }
                }
                row++;
            } catch (WriteException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        JoystickWrapper.getInstance().deleteObserver(this);
        try {
            sheet.addCell(new Number(6, 1, Main.getView().getTic()));
            //sheet.addCell(new Number(3, 1, Main.getView().getUnderstood()));
            if (Main.getView().getUnderstood() == 2) {
                sheet.addCell(new Number(3, 1, 2));
                sheet.addCell(new Number(4, 1, Main.getView().getPressingButton()));
            } else {
                sheet.addCell(new Number(3, 1, 1));
                sheet.addCell(new Number(4, 1, 1));
            }
            workbook.write();
            workbook.close();
        } catch (IOException | WriteException e) {
            e.printStackTrace();
        }
//        if (writer != null) {
//            writer.close();
//        }
    }
}
