/*
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;


final class FDate
{
    public byte Day;
    public byte Month;
    public int Year;

    FDate(String Source)
    {
        FromString(Source);
    }

    FDate(int Y, int M, int D)
    {
        Year = Y;
        Month = (byte)M;
        Day = (byte)D;
    }

    public long ToInt64()
    {
        long DayValue = (long)Day;
        long MonthValue = (long)Month << 8;
        long YearValue = (long)Year << 16;
        return DayValue | MonthValue | YearValue;
    }

    public void FromString(String Source)
    {
        String[] Dates = Source.split("/");

        if(Dates.length != 3)
        {
            throw new IllegalArgumentException();
        }

        String DayStr =  Dates[2];
        String MonthStr = Dates[1];
        String YearStr = Dates[0];

        Year = (int)Integer.parseUnsignedInt(YearStr);
        Month = (byte)Integer.parseUnsignedInt(MonthStr);
        Day = (byte)Integer.parseUnsignedInt(DayStr);

        if(Month > 12 || Day > 31)
        {
            throw new IllegalArgumentException();
        }
    }

    @Override public String toString()
    {
        return Integer.toString(Year)
        + "/" + Integer.toString(Month)
        + "/" + Integer.toString(Day);
    }

    public boolean Equals(FDate Other)
    {
        return ToInt64() == Other.ToInt64();
    }
    public boolean LessOrEqual(FDate Other)
    {
        return ToInt64() <= Other.ToInt64();
    }
    public boolean MoreOrEqual(FDate Other)
    {
        return ToInt64() >= Other.ToInt64();
    }
    public boolean More(FDate Other)
    {
        return ToInt64() > Other.ToInt64();
    }
    public boolean Less(FDate Other)
    {
        return ToInt64() < Other.ToInt64();
    }
}

class FBooking
{
    public FDate HiredFrom;
    public FDate HiredTo;

    FBooking(FDate From, FDate To)
    {
        if(From.MoreOrEqual(To))
        {
            throw new IllegalArgumentException("from is more than to");
        }

        HiredFrom = From;
        HiredTo = To;
    }

    @Override public String toString()
    {
        return HiredFrom + " - " + HiredTo;
    }
}

class Vehicle extends Object
{
    public ArrayList<FBooking> Bookings;
    public ArrayList<FBooking> GetBookings() {return Bookings;}

    public String RegNumber;
    public String Model;
    public String Color;

    static final public ArrayList<String> Models = InitModels();
    static final public ArrayList<String> Colors = InitColors();

    static private ArrayList<String> InitModels()
    {
        ArrayList<String> Result = new ArrayList<String>();
        Result.add("Ford");
        Result.add("Lamborghini");
        Result.add("Volvo");
        Result.add("BMW");
        Result.add("Ferrari");
        Result.add("Peugeot");
        return Result;
    }

    static private ArrayList<String> InitColors()
    {
        ArrayList<String> Result = new ArrayList<String>();
        Result.add("Red");
        Result.add("Black");
        Result.add("White");
        Result.add("Pink");
        Result.add("Brown");
        Result.add("Blue");
        return Result;
    }

    public Vehicle(String InModel, String InColor, String InRegNumber)
    {
        Bookings = new ArrayList<FBooking>();
        RegNumber = InRegNumber;
        Model = InModel;
        Color = InColor;
    }

    public Vehicle(Vehicle Other)
    {
        Bookings = Other.Bookings;
        RegNumber = Other.RegNumber;
        Model = Other.Model;
        Color = Other.Color;
    }

    public String toString()
    {
        return Model + " (" + Color + ") " + "[" + RegNumber + "]";
    }

    public boolean AddBooking(FBooking NewBooking)
    {
        int BookingIndex = FindNewBookingIndex(NewBooking.HiredFrom, NewBooking.HiredTo);

        if(BookingIndex != -1)
        {
            Bookings.add(BookingIndex, NewBooking);
            return true;
        }

        return false;
    }

    private int FindNewBookingIndex(FDate From, FDate To)
    {
        if(Bookings.isEmpty())
        {
            return 0;
        }

        for(int Index = 0; Index < Bookings.size(); ++Index)
        {
            FDate ExistingFrom = Bookings.get(Index).HiredFrom;
            FDate ExistingTo = Bookings.get(Index).HiredTo;

            boolean CanFitBefore = ExistingFrom.MoreOrEqual(To) && ((Index == 0) || From.MoreOrEqual(Bookings.get(Index - 1).HiredTo));
            boolean CanFitAfter = ExistingTo.LessOrEqual(From);
            boolean CanFitBeforeNext = (Index == (Bookings.size() - 1)) || To.LessOrEqual(Bookings.get(Index + 1).HiredFrom);

            if(CanFitAfter && CanFitBeforeNext)
            {
                return Index + 1;
            }
            else if(CanFitBefore)
            {
                return Index;
            }
        }

        return -1;
    }
}

class Car extends Vehicle
{
    enum EGearMode
    {
        Manual,
        Automatic
    }

    public EGearMode GearMode;

    public Car(String InModel, String InColor, String InRegNumber, EGearMode InGearMode)
    {
        super(InModel, InColor, InRegNumber);
        GearMode = InGearMode;
    }

    public Car(Vehicle Base, EGearMode InGearMode)
    {
        super(Base);
        GearMode = InGearMode;
    }

    public String toString()
    {
        return super.toString() + " " +  GearMode.name();
    }
}

class Bike extends Vehicle
{
    public int NumGears;

    public Bike(String InModel, String InColor, String InRegNumber, int InNumGears)
    {
        super(InModel, InColor, InRegNumber);
        NumGears = InNumGears;
    }

    public Bike(Vehicle Base, int InNumGears)
    {
        super(Base);
        NumGears = InNumGears;
    }

    public String toString()
    {
        return super.toString() + " " + NumGears + " gears";
    }
}

class Lorry extends Vehicle
{
    public float MaxLoad;

    public Lorry(String InModel, String InColor, String InRegNumber, float InMaxLoad)
    {
        super(InModel, InColor, InRegNumber);
        MaxLoad = InMaxLoad;
    }

    public Lorry(Vehicle Base, float InMaxLoad)
    {
        super(Base);
        MaxLoad = InMaxLoad;
    }

    public String toString()
    {
        return super.toString() + " " + MaxLoad + " tonnes";
    }
}

class VehicleSearchThread extends Thread
{
    VehicleManager Context;
    String SearchInput;
    String TypeOption;

    VehicleSearchThread(VehicleManager Context, String SearchInput, String TypeOption)
    {
        this.Context = Context;
        this.SearchInput = SearchInput;
        this.TypeOption = TypeOption;
        setDaemon(true);
    }

    public void run()
    {
        DefaultListModel<Vehicle> SortedCars = Context.SearchVehicles(SearchInput, TypeOption);

        synchronized(Context.VehicleList)
        {
            Context.VehicleList.setModel(SortedCars);
        }
    }
}

public class VehicleManager implements ActionListener, ListSelectionListener, KeyListener, WindowListener
{
    /** vars */
    private File VehicleFile;
    private JFrame Frame;
    public JList<Vehicle> VehicleList;
    private int SelectedVehicleIndex = -1;
    private DefaultListModel<Vehicle> VehicleArray;
    private JScrollPane CarPane;
    private JTextField SearchField;
    private JList<String> TypeList;
    private DefaultListModel<String> TypeArray;
    private JPanel SearchPanel;
    private JTextArea InfoArea;
    private JPanel InfoPanel;
    private JButton InfoClose;
    private JTextField AddBookingField_From;
    private JTextField AddBookingField_To;
    private JPanel AddBookingPanel;
    private JButton TryBookButton;

    private VehicleSearchThread SearchThread;
    /** end vars */

    public VehicleManager(int NumToGenerate, File VehicleFile) throws IOException
    {
        SearchThread = null;
        this.VehicleFile = VehicleFile;
        StartWindow(NumToGenerate);
    }

    @Override public void actionPerformed(ActionEvent Event)
    {
        if(Event.getSource() == InfoClose)
        {
            OnCloseInfo();
        }
        else if(Event.getSource() == TryBookButton)
        {
            OnTryBook();
        }
    }

    @Override public void valueChanged(ListSelectionEvent Event)
    {
        if(!Event.getValueIsAdjusting())
        {
            if(Event.getSource() == VehicleList && VehicleList.getSelectedIndex() != -1)
            {
                DisplayCar(VehicleList.getSelectedIndex());
            }
            else if(Event.getSource() == TypeList)
            {
                AsyncSearch();
            }
        }
    }

    @Override public void keyTyped(KeyEvent e)
    {
    }

    @Override public void keyPressed(KeyEvent e)
    {
    }

    @Override public void keyReleased(KeyEvent Event)
    {
        AsyncSearch();
    }

    @Override public void windowOpened(WindowEvent e) {}

    @Override public void windowClosing(WindowEvent e)
    {
        try
        {
            SaveToFile();
        }
        catch(IOException ignored){}
    }

    @Override public void windowClosed(WindowEvent e) {}
    @Override public void windowIconified(WindowEvent e) {}
    @Override public void windowDeiconified(WindowEvent e) {}
    @Override public void windowActivated(WindowEvent e) {}
    @Override public void windowDeactivated(WindowEvent e) {}

    private void AsyncSearch()
    {
        if(SearchThread != null && SearchThread.isAlive())
        {
            SearchThread.stop();
        }

        SearchThread = new VehicleSearchThread(this, SearchField.getText(), TypeList.getSelectedValue());
        SearchThread.start();
    }

    private void OnCloseInfo()
    {
        Frame.remove(InfoPanel);

        Frame.repaint();
        Frame.revalidate();
    }

    private void OnTryBook()
    {
        Vehicle SelectedCar = VehicleList.getModel().getElementAt(SelectedVehicleIndex);

        FDate From = null;
        FDate To = null;

        try
        {
            From = new FDate(AddBookingField_From.getText());
        }
        catch(IllegalArgumentException Exception)
        {
            AddBookingField_From.setText("!invalid date!");
            From = null;
        }

        try
        {
            To = new FDate(AddBookingField_To.getText());
        }
        catch(IllegalArgumentException Exception)
        {
            AddBookingField_To.setText("!invalid date!");
            To = null;
        }

        if(From == null || To == null)
        {
            return;
        }

        FBooking Booking = null;

        try
        {
            Booking = new FBooking(From, To);
        }
        catch(IllegalArgumentException Exception)
        {
            AddBookingField_From.setText("!invalid date!");
            AddBookingField_To.setText("!invalid date!");
            return;
        }

        if(!SelectedCar.AddBooking(Booking))
        {
            AddBookingField_From.setText("that date is not available");
            AddBookingField_To.setText("that date is not available");
            return;
        }

        DisplayCar(SelectedVehicleIndex);
    }

    private void DisplayCar(int CarIndex)
    {
        SelectedVehicleIndex = CarIndex;
        Vehicle SelectedCar = VehicleList.getModel().getElementAt(SelectedVehicleIndex);

        Frame.remove(InfoPanel);

        InfoArea.setText(SelectedCar.toString());

        for(FBooking Booking : SelectedCar.GetBookings())
        {
            InfoArea.append("\n" + Booking.toString());
        }

        Frame.add(InfoPanel, BorderLayout.SOUTH);

        Frame.repaint();
        Frame.revalidate();
    }

    private static String RandomizeRegNumber()
    {
        Random RandomGenerator = new Random();

        char Letter1 = (char)RandomGenerator.nextInt(65, 90);
        char Letter2 = (char)RandomGenerator.nextInt(65, 90);
        char Letter3 = (char)RandomGenerator.nextInt(65, 90);

        char Number1 = (char)RandomGenerator.nextInt(48, 57);
        char Number2 = (char)RandomGenerator.nextInt(48, 57);
        char Number3 = (char)RandomGenerator.nextInt(48, 57);

        return "" + Letter1 + "" + Letter2 + "" + Letter3 + " " + Number1 + "" + Number2 + "" + Number3;
    }

    private static String RandomizeModelName()
    {
        Random RandomGenerator = new Random();

        int ModelNameOffset = RandomGenerator.nextInt(0, Vehicle.Models.size() - 1);
        return Vehicle.Models.get(ModelNameOffset);
    }

    private static String RandomizeColor()
    {
        Random RandomGenerator = new Random();

        int ModelNameOffset = RandomGenerator.nextInt(0, Vehicle.Colors.size() - 1);
        return Vehicle.Colors.get(ModelNameOffset);
    }

    private static Car.EGearMode RandomizeGearMode()
    {
        Random RandomGenerator = new Random();

        int Mode = RandomGenerator.nextInt(0, 2);
        return Car.EGearMode.values()[Mode];
    }

    private static int RandomizeNumGears()
    {
        Random RandomGenerator = new Random();
        return RandomGenerator.nextInt(1, 12);
    }

    private static float RandomizeMaxLoad()
    {
        Random RandomGenerator = new Random();

        int MaxInt =  RandomGenerator.nextInt(1, 20);
        float MaxFloat = MaxInt;
        MaxFloat /= 1.5;
        return MaxFloat;
    }

    private void PopulateVehicles(int NumToGenerate)
    {
        Random RandomGenerator = new Random();

        for(int Index = 0; Index < NumToGenerate; ++Index)
        {
            Vehicle Base = new Vehicle(RandomizeModelName(), RandomizeColor(), RandomizeRegNumber());

            int VehicleType = RandomGenerator.nextInt(0, 3);

            switch(VehicleType)
            {
                case 0 -> VehicleArray.addElement(new Car(Base, RandomizeGearMode()));
                case 1 -> VehicleArray.addElement(new Bike(Base, RandomizeNumGears()));
                case 2 -> VehicleArray.addElement(new Lorry(Base, RandomizeMaxLoad()));
            }
        }
    }

    private static Vehicle ParseVehicle(Scanner Scan)
    {
        String Line = Scan.nextLine();

        String[] Model_Color_RegNumber = Line.split(" ", 3);

        String Model = Model_Color_RegNumber[0];

        String Color = Model_Color_RegNumber[1];
        Color = Color.substring(1, Color.length() - 1);

        String RegNumber = Model_Color_RegNumber[2];
        RegNumber = RegNumber.substring(1, RegNumber.indexOf(']'));

        Vehicle Base = new Vehicle(Model, Color, RegNumber);

        String SpecialMember = Line.split(" ")[4];

        if(Line.contains("gears"))
        {
            return new Bike(Base, Integer.parseUnsignedInt(SpecialMember));
        }
        else if(Line.contains("tonnes"))
        {
            return new Lorry(Base, Float.parseFloat(SpecialMember));
        }
        else
        {
            return new Car(Base, Car.EGearMode.valueOf(SpecialMember));
        }
    }

    private static ArrayList<FBooking> ParseBookings(Scanner Scan)
    {
        ArrayList<FBooking> Result = new ArrayList<FBooking>();

        Scan.nextLine();

        for(String Line = Scan.nextLine(); !Line.equals("}"); Line = Scan.nextLine())
        {
            String[] Dates = Line.split("-");
            FDate From = new FDate(Dates[0].substring(0, Dates[0].length() - 1));
            FDate To = new FDate(Dates[1].substring(1, Dates[1].length()));
            FBooking Booking = new FBooking(From, To);
            Result.add(Booking);
        }

        return Result;
    }

    private void PopulateVehicles() throws IOException
    {
        FileReader Reader = new FileReader(VehicleFile);
        Scanner Scan = new Scanner(Reader);

        while(Scan.hasNextLine())
        {
            Vehicle ParsedVehicle = ParseVehicle(Scan);
            ParsedVehicle.Bookings = ParseBookings(Scan);
            VehicleArray.addElement(ParsedVehicle);
        }

        Reader.close();
    }

    private void SaveToFile() throws IOException
    {
        FileWriter Writer = new FileWriter(VehicleFile, false);

        for(int Index = 0; Index < VehicleArray.size(); ++Index)
        {
            Vehicle Car = VehicleArray.elementAt(Index);
            Writer.write(Car.toString() + "\n");

            Writer.write("{\n");

            for(FBooking Booking : Car.Bookings)
            {
                Writer.write(Booking.toString() + "\n");
            }

            Writer.write("}\n");
        }

        Writer.close();
    }

    private void StartWindow(int NumCars) throws IOException
    {
        Frame = new JFrame("Vehicle Rentals");
        Frame.setSize(500, 500);
        Frame.setLayout(new BorderLayout());
        Frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Frame.addWindowListener(this);

        VehicleArray = new DefaultListModel<Vehicle>();

        if(NumCars != -1)
        {
            PopulateVehicles(NumCars);
        }
        else
        {
            PopulateVehicles();
        }

        VehicleList = new JList<Vehicle>(VehicleArray);
        VehicleList.addListSelectionListener(this);

        CarPane = new JScrollPane(VehicleList);

        SearchField = new JTextField();
        SearchField.addKeyListener(this);

        SearchPanel = new JPanel(new GridLayout(1, 2));

        TypeArray = new DefaultListModel<String>();
        TypeArray.addElement("Car");
        TypeArray.addElement("Lorry");
        TypeArray.addElement("Bike");

        TypeList = new JList<String>(TypeArray);
        TypeList.addListSelectionListener(this);

        SearchPanel.add(SearchField);
        SearchPanel.add(TypeList);

        Frame.add(CarPane, BorderLayout.CENTER);
        Frame.add(SearchPanel, BorderLayout.NORTH);
        Frame.setVisible(true);

        InfoArea = new JTextArea();
        InfoClose = new JButton("CLOSE");
        InfoPanel = new JPanel(new BorderLayout());
        AddBookingField_From = new JTextField(35);
        AddBookingField_To = new JTextField(35);
        AddBookingPanel = new JPanel(new GridLayout(3, 1));
        TryBookButton = new JButton("Book!");

        InfoArea.setEditable(false);
        InfoClose.addActionListener(this);
        TryBookButton.addActionListener(this);

        AddBookingPanel.add(AddBookingField_From);
        AddBookingPanel.add(AddBookingField_To);
        AddBookingPanel.add(TryBookButton);

        InfoPanel.add(AddBookingPanel, BorderLayout.WEST);
        InfoPanel.add(InfoArea, BorderLayout.CENTER);
        InfoPanel.add(InfoClose, BorderLayout.EAST);

        TypeList.setSelectedIndex(0);
    }

    public DefaultListModel<Vehicle> SearchVehicles(String SearchInput, String TypeOption)
    {
        final String UppercaseInput = SearchInput.toUpperCase();

        DefaultListModel<Vehicle> Searched = new DefaultListModel<Vehicle>();

        for(int Index = 0; Index < VehicleArray.size(); ++Index)
        {
            String CarAsString = VehicleArray.elementAt(Index).toString();
            CarAsString = CarAsString.toUpperCase();

            if(CarAsString.contains(UppercaseInput) && TypeOption.equals(VehicleArray.elementAt(Index).getClass().getName()))
            {
                Searched.addElement(VehicleArray.elementAt(Index));
            }
        }

        return Searched;
    }
}

class Main
{
    public static void main(String[] args) throws Throwable
    {
        File VehicleFile = new File("Vehicles.txt");

        new VehicleManager(VehicleFile.exists() ? -1 : 100, VehicleFile);
    }
}
