/*
The MIT License (MIT)

Copyright (c) 2016 Bill Femmer

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package com.bfemmer.fgslogs.infrastructure;

import com.bfemmer.fgslogs.model.Formation;
import com.bfemmer.fgslogs.model.FormationEntity;
import com.bfemmer.fgslogs.model.Location;
import com.bfemmer.fgslogs.model.LocationEntity;
import com.bfemmer.fgslogs.viewmodel.LookupCodes;
import com.bfemmer.fgslogs.model.Mineral;
import com.bfemmer.fgslogs.model.Sample;
import com.bfemmer.fgslogs.model.SampleEntity;
import com.bfemmer.fgslogs.model.Summary;
import com.bfemmer.fgslogs.model.SummaryEntity;
import com.bfemmer.fgslogs.model.WellLog;
import com.bfemmer.fgslogs.model.WellLogRepository;
import com.bfemmer.fgslogs.model.WellNumberEntity;
import com.bfemmer.fgslogs.viewmodel.FormationViewModel;
import com.bfemmer.fgslogs.viewmodel.LatLng;
import com.bfemmer.fgslogs.viewmodel.LocationViewModel;
import com.bfemmer.fgslogs.viewmodel.SampleViewModel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bfemmer
 */
public class DatFileWellLogRepository implements WellLogRepository {
    // Record types
    private static final String START_OF_WELL_RECORD = "1";
    private static final String OWNER_DRILLER_RECORD = "2";
    private static final String WORKED_BY_RECORD = "3";
    private static final String FORMATION_RECORD = "4";
    private static final String SAMPLE_RECORD = "6";
    private static final String END_OF_WELL_RECORD = "9";
    
    // Record 1
    private static final int WELL_NUMBER_BEGIN_INDEX = 1;
    private static final int WELL_NUMBER_END_INDEX = 6;
    private static final int BOT_DEPTH_BEGIN_INDEX = 6;
    private static final int BOT_DEPTH_END_INDEX = 11;
    private static final int COUNTY_BEGIN_INDEX = 11;
    private static final int COUNTY_END_INDEX = 13;
    private static final int TOWNSHIP_BEGIN_INDEX = 13;
    private static final int TOWNSHIP_END_INDEX = 16;
    private static final int RANGE_BEGIN_INDEX = 16;
    private static final int RANGE_END_INDEX = 19;
    private static final int SECTION_BEGIN_INDEX = 19;
    private static final int SECTION_END_INDEX = 21;
    private static final int QTRSECTION_BEGIN_INDEX = 21;
    private static final int QTRSECTION_END_INDEX = 23;
    private static final int LAT_DEG_BEGIN_INDEX = 23;
    private static final int LAT_DEG_END_INDEX = 25;
    private static final int LAT_MIN_BEGIN_INDEX = 25;
    private static final int LAT_MIN_END_INDEX = 27;
    private static final int LAT_SEC_BEGIN_INDEX = 27;
    private static final int LAT_SEC_END_INDEX = 29;
    private static final int LNG_DEG_BEGIN_INDEX = 29;
    private static final int LNG_DEG_END_INDEX = 31;
    private static final int LNG_MIN_BEGIN_INDEX = 31;
    private static final int LNG_MIN_END_INDEX = 33;
    private static final int LNG_SEC_BEGIN_INDEX = 33;
    private static final int LNG_SEC_END_INDEX = 35;
    private static final int TOTAL_DEPTH_BEGIN_INDEX = 35;
    private static final int TOTAL_DEPTH_END_INDEX = 40;
    private static final int ELEVATION_BEGIN_INDEX = 40;
    private static final int ELEVATION_END_INDEX = 43;
    private static final int SAMPLES_BEGIN_INDEX = 43;
    private static final int SAMPLES_END_INDEX = 46;
    private static final int FROM_DEPTH_BEGIN_INDEX = 46;
    private static final int FROM_DEPTH_END_INDEX = 51;
    private static final int TO_DEPTH_BEGIN_INDEX = 51;
    private static final int TO_DEPTH_END_INDEX = 56;
    private static final int YEAR_BEGIN_INDEX = 56;
    private static final int YEAR_END_INDEX = 58;
    private static final int MONTH_BEGIN_INDEX = 58;
    private static final int MONTH_END_INDEX = 60;
    private static final int DAY_BEGIN_INDEX = 60;
    private static final int DAY_END_INDEX = 62;
    
    // Record 4
    private static final int FM_FROM_DEPTH_BEGIN_INDEX = 6;
    private static final int FM_FROM_DEPTH_END_INDEX = 11;
    private static final int FM_TO_DEPTH_BEGIN_INDEX = 13;
    private static final int FM_TO_DEPTH_END_INDEX = 18;
    private static final int FM_CODE_BEGIN_INDEX = 20;
    private static final int FM_CODE_END_INDEX = 27;
    
    private static final int DATA_OFFSET_INDEX = 6;
    
    private static final int SAMPLE_TO_DEPTH_BEGIN_INDEX = 6;
    private static final int SAMPLE_TO_DEPTH_END_INDEX = 11;
    private static final int ROCK_TYPE_BEGIN_INDEX = 12;
    private static final int ROCK_TYPE_END_INDEX = 13;
    private static final int SAMPLE_COMMENT_BEGIN_INDEX = 13;
    private static final int ROCK_COLOR_BEGIN_INDEX = 13;
    private static final int ROCK_COLOR_END_INDEX = 15;
    private static final int ROCK_COLOR2_BEGIN_INDEX = 15;
    private static final int ROCK_COLOR2_END_INDEX = 17;
    
    private static final int POROSITY_BEGIN_INDEX = 17;
    private static final int POROSITY_END_INDEX = 19;
    private static final int POROSITY_CODE_BEGIN_INDEX = 19;
    private static final int POROSITY_CODE_END_INDEX = 22;
    
    private static final int GRAIN_SIZE_BEGIN_INDEX = 22;
    private static final int GRAIN_SIZE_END_INDEX = 23;
    private static final int RANGE_MIN_BEGIN_INDEX = 23;
    private static final int RANGE_MIN_END_INDEX = 24;
    private static final int RANGE_MAX_BEGIN_INDEX = 24;
    private static final int RANGE_MAX_END_INDEX = 25;
    
    private static final int ROUNDNESS_MIN_BEGIN_INDEX = 25;
    private static final int ROUNDNESS_MIN_END_INDEX = 26;
    private static final int ROUNDNESS_MAX_BEGIN_INDEX = 26;
    private static final int ROUNDNESS_MAX_END_INDEX = 27;
    private static final int SPHERICITY_BEGIN_INDEX = 27;
    private static final int SPHERICITY_END_INDEX = 28;

    private static final int GRAIN_TYPE_BEGIN_INDEX = 28;
    private static final int GRAIN_TYPE_END_INDEX = 31;
    private static final int LIMESTONE_GRAIN_SIZE_BEGIN_INDEX = 33;
    private static final int LIMESTONE_GRAIN_SIZE_END_INDEX = 34;
    private static final int LIMESTONE_RANGE_MIN_BEGIN_INDEX = 34;
    private static final int LIMESTONE_RANGE_MIN_END_INDEX = 35;
    private static final int LIMESTONE_RANGE_MAX_BEGIN_INDEX = 35;
    private static final int LIMESTONE_RANGE_MAX_END_INDEX = 36;
    
    private static final int ALTERATION_BEGIN_INDEX = 36;
    private static final int ALTERATION_END_INDEX = 37;
    private static final int CRYSTALLINITY_BEGIN_INDEX = 37;
    private static final int CRYSTALLINITY_END_INDEX = 38;
    
    private static final int DOLOMITE_GRAIN_SIZE_BEGIN_INDEX = 38;
    private static final int DOLOMITE_GRAIN_SIZE_END_INDEX = 39;
    private static final int DOLOMITE_RANGE_MIN_BEGIN_INDEX = 39;
    private static final int DOLOMITE_RANGE_MIN_END_INDEX = 40;
    private static final int DOLOMITE_RANGE_MAX_BEGIN_INDEX = 40;
    private static final int DOLOMITE_RANGE_MAX_END_INDEX = 41;
    
    private static final int INDURATION_BEGIN_INDEX = 41;
    private static final int INDURATION_END_INDEX = 42;
    private static final int CEMENTS_BEGIN_INDEX = 42;
    private static final int CEMENTS_END_INDEX = 45;
    private static final int SEDIMENTARY_BEGIN_INDEX = 45;
    private static final int SEDIMENTARY_END_INDEX = 49;
    
    private static final int MINERALS_BEGIN_INDEX = 50;
    private static final int MINERALS_END_INDEX = 62;
    
    private static final int FEATURES_BEGIN_INDEX = 62;
    private static final int FEATURES_END_INDEX = 67;
    private static final int FOSSILS_INDEX = 67;
    
    private final String directory;
    
    public DatFileWellLogRepository(String directory) {
        this.directory = directory;
    }

    @Override
    public List<WellLog> getAllWellLogs(String filename) {
        Reader fileReader;
        List<WellLog> wellLogs = null;
        
        try {
            fileReader = new FileReader(filename);
            wellLogs = parseDatFileReader(fileReader);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DatFileWellLogRepository.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return wellLogs;
    }

    @Override
    public List<WellNumberEntity> getWellNumbersByFile(String filename) {
        Reader fileReader;
        List<String> wellNumbers;
        List<WellNumberEntity> wellNumberEntities = new ArrayList<>();
        
        try {
            fileReader = new FileReader(filename);
            
            // Get list of well numbers from file
            wellNumbers = getWellNumbersFromReader(fileReader);

            // We have to count the frequency of occurence of well numbers
            // because some well numbers have more than one log.
            Set<String> uniqueSet = new HashSet<>(wellNumbers);
            for (String wellNumber : uniqueSet) {
                    wellNumberEntities.add(new WellNumberEntity(wellNumber, Collections.frequency(wellNumbers, wellNumber)));
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(DatFileWellLogRepository.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Sort the list by well number
        Collections.sort(wellNumberEntities, new SortByWellNumber());
                
        return wellNumberEntities;
    }

    @Override
    public List<WellNumberEntity> getWellNumbersByCounty(String county) {
        Reader fileReader;
        List<String> wellNumbers;
        List<WellNumberEntity> wellNumberEntities = new ArrayList<>();
        File[] datFiles = new File(directory).listFiles();
        
        for (File datFile : datFiles) {
            try {
                fileReader = new FileReader(datFile);
                
                String datCounty = getCountyFromReader(fileReader);
                if (!datCounty.equals(county)) {
                    continue;
                }
                
                // Reset reader (seek) to beginning of file
                fileReader.close();
                fileReader = null;
                fileReader = new FileReader(datFile);
                
                // Get list of well numbers from file
                wellNumbers = getWellNumbersFromReader(fileReader);
                
                // We have to count the frequency of occurence of well numbers
                // because some well numbers have more than one log.
                Set<String> uniqueSet = new HashSet<>(wellNumbers);
                for (String wellNumber : uniqueSet) {
                        wellNumberEntities.add(new WellNumberEntity(wellNumber, Collections.frequency(wellNumbers, wellNumber)));
                }
                
                break;
            } catch (FileNotFoundException ex) {
                Logger.getLogger(DatFileWellLogRepository.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(DatFileWellLogRepository.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        // Sort the list by well number
        Collections.sort(wellNumberEntities, new SortByWellNumber());
                
        return wellNumberEntities;
    }

    @Override
    public WellLog getWellLogById(String id) {
        WellLog foundWellLog = null;
        
        for (File datFile : new File(directory).listFiles()) {
            // Load logs in file
            List<WellLog> wellLogs = getAllWellLogs(datFile.toString());
            
            // Look for log of interest
            for (WellLog wellLog : wellLogs) {
                if (wellLog.getId().equals(id)) {
                    foundWellLog = wellLog;
                    break;
                }
            }
        }
        
        return foundWellLog;
    }

    @Override
    public List<WellLog> getWellLogByWellNumber(String wellNumber) {
        List<WellLog> logs = new ArrayList<>();
        
        for (File datFile : new File(directory).listFiles()) {
            try {
                // Get list of well numbers from file and look for the 
                // desired number in the list. If the number is not there,
                // continue with the next file.
                if (!getWellNumbersFromReader(
                        new FileReader(datFile)).contains(wellNumber)) {
                    continue;
                }
                
                // Import the file into a list of well logs
                List<WellLog> wellLogs = getAllWellLogs(datFile.toString());
                
                // Look for log of interest and add it to the list to be returned
                for (WellLog wellLog : wellLogs) {
                    if (wellLog.getWellNumber().equals(wellNumber)) {
                        logs.add(wellLog);
                    }
                }
                
                // Break out of for loop ... no need to look in additional files
                break;
            } catch (FileNotFoundException ex) {
                Logger.getLogger(DatFileWellLogRepository.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return logs;
    }

    @Override
    public LatLng getLatLngByWellNumber(String wellNumber) {
        LatLng latLng = null;
        
        for (File datFile : new File(directory).listFiles()) {
            // Load logs in file
            List<WellLog> wellLogs = getAllWellLogs(datFile.toString());
            
            // Look for log of interest
            for (WellLog wellLog : wellLogs) {
                if (wellLog.getWellNumber().equals(wellNumber)) {
                    latLng = wellLog.getLocationEntity().getLocation().getLatLong();
                    break;
                }
            }
        }
        
        return latLng;
    }

    @Override
    public void saveWellLog(WellLog wellLog) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void saveWellLogs(List<WellLog> wellLogs) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private String getCountyFromReader(Reader reader) {
        BufferedReader bufferedReader = new BufferedReader(reader);
        String countyCode = "";
        LookupCodes codes = new LookupCodes();
        
        try {
            String record = bufferedReader.readLine();
            countyCode = record.substring(COUNTY_BEGIN_INDEX, COUNTY_END_INDEX).trim();
        } catch (IOException ex) {
            Logger.getLogger(DatFileWellLogRepository.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return codes.getCountyCodeMap().get(countyCode);
    }
    
    private List<String> getWellNumbersFromReader(Reader reader) {
        BufferedReader bufferedReader = new BufferedReader(reader);
        String recordId;
        String currentLine;
        String wellNumber;
        List<String> wellNumbers = new ArrayList<>();
        
        try {
            while ((currentLine = bufferedReader.readLine()) != null) {
                // Check for empty lines
                if (currentLine.length() == 0) continue;
                
                // Check for empty lines
                if (currentLine.length() < 7) continue;
                
                // Get record ID from string
                recordId = currentLine.substring(0, 1);
                                    
                switch (recordId) {
                    case START_OF_WELL_RECORD:
                        wellNumber = currentLine.substring(
                                WELL_NUMBER_BEGIN_INDEX, WELL_NUMBER_END_INDEX).trim();
                        wellNumbers.add(wellNumber);
                        break;
                    default:
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(DatFileWellLogRepository.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return wellNumbers;
    }
    
    public List<WellLog> parseDatFileReader(Reader reader) {
        BufferedReader bufferedReader = new BufferedReader(reader);
        String recordId;
        String currentLine;
        WellLog wellLog;
        Summary summary;
        Location location;
        List<Sample> samples;
        List<FormationViewModel> formations;
        List<WellLog> wellLogs;
        
        wellLog = new WellLog();
        summary = new Summary();
        location = new Location();
        samples = new ArrayList<>();
        formations = new ArrayList<>();
        wellLogs = new ArrayList<>();
        
        try {
            
            while ((currentLine = bufferedReader.readLine()) != null) {
                // Check for empty lines
                if (currentLine.length() == 0) continue;
                
                // Get record ID from string
                recordId = currentLine.substring(0, 1);
                                    
                switch (recordId) {
                    case START_OF_WELL_RECORD:
                        wellLog = new WellLog();
                        summary = new Summary();
                        samples = new ArrayList<>();
                        formations = new ArrayList<>();
                        location = new Location();
                        wellLog.setId(UUID.randomUUID().toString());
                        
                        parseHeaderIntoWellLog (currentLine, wellLog, summary);
                        parseLocationIntoWellLog (currentLine, location);
                        parseDateIntoSummary (currentLine, summary);
                        break;
                    case OWNER_DRILLER_RECORD:
                        parseOwnerDrillerIntoSummary (currentLine, summary);
                        break;
                    case WORKED_BY_RECORD:
                        parseWorkedByIntoSummary (currentLine, summary);
                        break;
                    case FORMATION_RECORD:
                        parseFormation (currentLine, formations);
                        break;
                    case SAMPLE_RECORD:
                        parseSample (currentLine, samples);
                        break;
                    case END_OF_WELL_RECORD:
                    default:
                        // Process samples
                        SampleEntity sampleEntity = new SampleEntity(
                            UUID.randomUUID().toString(), wellLog.getId(), wellLog.getWellNumber());
                        
                        samples.stream().map((sample) -> new SampleViewModel(
                                sample)).forEachOrdered((sampleView) -> {
                                    sampleEntity.getSamples().add(sampleView);
                        });
                        
                        // Override the sample count from the header with
                        // the actual count in the list above
                        summary.setSampleCount(samples.size());
                        
                        // Process formations
                        FormationEntity formationEntity = new FormationEntity(
                            UUID.randomUUID().toString(), wellLog.getId(), wellLog.getWellNumber());
                        
                        formationEntity.setFormations(formations);
                        
                        // Process location
                        LocationViewModel locationViewModel = new LocationViewModel(location);
                        LocationEntity locationEntity = new LocationEntity(
                            UUID.randomUUID().toString(), wellLog.getId(), wellLog.getWellNumber());
                        locationEntity.setLocation(locationViewModel);
                        
                        // Process summary
                        SummaryEntity summaryEntity = new SummaryEntity(
                            UUID.randomUUID().toString(), wellLog.getId(), wellLog.getWellNumber());
                        summaryEntity.setSummary(summary);
                        
                        // Add entities to wellLog
                        wellLog.setSampleEntity(sampleEntity);
                        wellLog.setFormationEntity(formationEntity);
                        wellLog.setLocationEntity(locationEntity);
                        wellLog.setSummaryEntity(summaryEntity);
                        
                        wellLogs.add(wellLog);
                        break;
                }
            }
            
        } catch (IOException e) {
            Logger.getLogger(DatFileWellLogRepository.class.getName()).log(Level.SEVERE, null, e);
        } catch (Exception exception) {
            Logger.getLogger(DatFileWellLogRepository.class.getName()).log(Level.SEVERE, null, exception);
        }finally {
            try {
                bufferedReader.close();
            } catch (IOException ex) {
                Logger.getLogger(DatFileWellLogRepository.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return wellLogs;
    }

    private void parseHeaderIntoWellLog(String line, WellLog wellLog, Summary summary) {
        String temp;
        
        try {
            temp = line.substring(WELL_NUMBER_BEGIN_INDEX, WELL_NUMBER_END_INDEX).trim();
            
            wellLog.setWellNumber(temp);
            
            System.out.println("------- Well Number: " + temp);
            
            temp = line.substring(BOT_DEPTH_BEGIN_INDEX, BOT_DEPTH_END_INDEX).trim();
            if (temp.length() > 0) summary.setBottomSampleDepth(Double.valueOf(temp));

            temp = line.substring(TOTAL_DEPTH_BEGIN_INDEX, TOTAL_DEPTH_END_INDEX).trim();
            try {
                if (temp.length() > 0) summary.setTotalDepth(Double.valueOf(temp));
            }
            catch(NumberFormatException nfe) {
                // TODO evaluate why the data is incorrect
            }

            if (line.length() >= ELEVATION_END_INDEX) {
                temp = line.substring(ELEVATION_BEGIN_INDEX, ELEVATION_END_INDEX).trim();
                
                try{
                    if (!temp.isEmpty()) summary.setElevation(Double.valueOf(temp));
                }
                catch(NumberFormatException nfe) {
                    // TODO evaluate why the data is incorrect
                }
            }
            
            // Note: will use the actual count of samples found instead of
            // the value found at SAMPLES_BEGIN_INDEX because many times
            // the value is either omitted or is incorrect. The setSampleCount
            // call is now made at the bottom of parseDatFileReader().
//            if (line.length() >= SAMPLES_END_INDEX) {
//                temp = line.substring(SAMPLES_BEGIN_INDEX, SAMPLES_END_INDEX).trim();
//                if (temp.length() > 0) wellLog.setSampleCount(Integer.valueOf(temp));
//            }
            
            if (line.length() >= FROM_DEPTH_END_INDEX) {
                temp = line.substring(FROM_DEPTH_BEGIN_INDEX, FROM_DEPTH_END_INDEX).replaceAll("\\s+","");
                try {
                    if (temp.length() > 0) summary.setFromDepth(Double.valueOf(temp));
                }
                catch(NumberFormatException nfe) {
                    summary.setFromDepth(0);
                }
            }
            
            if (line.length() >= TO_DEPTH_END_INDEX) {
                temp = line.substring(TO_DEPTH_BEGIN_INDEX, TO_DEPTH_END_INDEX).replaceAll("\\s+","");
                try {
                    if (temp.length() > 0) summary.setToDepth(Double.valueOf(temp));
                }
                catch(NumberFormatException nfe) {
                    summary.setFromDepth(0);
                }
            }
        }
        catch (Exception e) {
            Logger.getLogger(DatFileWellLogRepository.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    private void parseLocationIntoWellLog(String line, Location location) {
        try {
            location.setCountyCode(
                    line.substring(COUNTY_BEGIN_INDEX, COUNTY_END_INDEX).trim());
            
            // Township, Range, Section, and Quartersection
            parseTRSQ(line, location);
            
            // Lat/Long and Degrees, Minutes, and Seconds
            parseDMS(line, location);
            
        } catch (Exception e) {
            Logger.getLogger(DatFileWellLogRepository.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    private void parseTRSQ(String line, Location location) {
        String temp;
        
        // Prepend "0" to township if length is too short
        temp = line.substring(TOWNSHIP_BEGIN_INDEX, TOWNSHIP_END_INDEX).trim();
        if (temp.length() < 3) temp = "0" + temp;
        location.setTownship(temp);

        // Prepend "0" to range if length is too short
        temp = line.substring(RANGE_BEGIN_INDEX, RANGE_END_INDEX).trim();
        if (temp.length() < 3) temp = "0" + temp;
        location.setRange(temp);

        temp = line.substring(SECTION_BEGIN_INDEX, SECTION_END_INDEX).trim();
        if (temp.length() > 0) {
            try {
                location.setSection(Integer.valueOf(temp));
            }
            catch (NumberFormatException nfe) {
                // TODO Investigate
            }
        }
            

        location.setQuarterSection(
                line.substring(QTRSECTION_BEGIN_INDEX, QTRSECTION_END_INDEX).trim());
    }
    
    private void parseDMS(String line, Location location) {
        int latDegrees = 0;
        int latMinutes = 0;
        int latSeconds = 0;
        int lngDegrees = 0;
        int lngMinutes = 0;
        int lngSeconds = 0;
        String temp;
        
        temp = line.substring(LAT_DEG_BEGIN_INDEX, LAT_DEG_END_INDEX).trim();
        if (temp.length() > 0) latDegrees = Integer.valueOf(temp);
        location.setLatDegrees(latDegrees);

        temp = line.substring(LAT_MIN_BEGIN_INDEX, LAT_MIN_END_INDEX).trim();
        if (temp.length() > 0) latMinutes = Integer.valueOf(temp);
        location.setLatMinutes(latMinutes);

        temp = line.substring(LAT_SEC_BEGIN_INDEX, LAT_SEC_END_INDEX).trim();
        if (temp.length() > 0) latSeconds = Integer.valueOf(temp);
        location.setLatSeconds(latSeconds);

        temp = line.substring(LNG_DEG_BEGIN_INDEX, LNG_DEG_END_INDEX).trim();
        if (temp.length() > 0) lngDegrees = Integer.valueOf(temp);
        location.setLngDegrees(lngDegrees);

        temp = line.substring(LNG_MIN_BEGIN_INDEX, LNG_MIN_END_INDEX).trim();
        if (temp.length() > 0) lngMinutes = Integer.valueOf(temp);
        location.setLngMinutes(lngMinutes);

        temp = line.substring(LNG_SEC_BEGIN_INDEX, LNG_SEC_END_INDEX).trim();
        if (temp.length() > 0) lngSeconds = Integer.valueOf(temp);
        location.setLngSeconds(lngSeconds);
    }
    
    private void parseDateIntoSummary(String line, Summary summary) {
        String temp;
        
        try {
            if (line.length() > (YEAR_BEGIN_INDEX + 1)) {
                temp = line.substring(YEAR_BEGIN_INDEX, YEAR_END_INDEX).trim();
                if (temp.length() > 0)
                    summary.setCompletionDateYear(Integer.valueOf(temp));

                if (line.length() > (MONTH_BEGIN_INDEX + 1)) {
                    temp = line.substring(MONTH_BEGIN_INDEX, MONTH_END_INDEX).trim();
                    if (temp.length() > 0)
                        summary.setCompletionDateMonth(Integer.valueOf(temp));

                    if (line.length() > (DAY_BEGIN_INDEX + 1)) {
                        temp = line.substring(DAY_BEGIN_INDEX, DAY_END_INDEX).trim();
                        if (temp.length() > 0) {
                            try{
                                summary.setCompletionDateDay(Integer.valueOf(temp));
                            }
                            catch(NumberFormatException nfe) {

                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(DatFileWellLogRepository.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    private void parseOwnerDrillerIntoSummary(String line, Summary summary) {
        String ownerDriller = summary.getOwnerDriller();
        
        if (ownerDriller.length() > 0)
            summary.setOwnerDriller(ownerDriller + " " + line.substring(DATA_OFFSET_INDEX));
        else
            summary.setOwnerDriller(line.substring(DATA_OFFSET_INDEX));
    }
    
    private void parseWorkedByIntoSummary(String line, Summary summary) {
        String workedBy = summary.getWorkedBy();
        
        if (workedBy.length() > 0)
            summary.setWorkedBy(workedBy + " " + line.substring(DATA_OFFSET_INDEX));
        else
            summary.setWorkedBy(line.substring(DATA_OFFSET_INDEX));
    }
    
    private void parseFormation(String line, List<FormationViewModel> formations) {
        FormationViewModel previousFormation = null;
        Formation formation = new Formation();
        double lastToDepth = 0;
        String temp;
        
        // If only one depth is in the record, then the depth is the "to" depth
        if (line.length() <= (FM_FROM_DEPTH_END_INDEX + 1)) {
            // Get previous formation (if it exists) to get the last 
            // "to depth" value and overwrite "lastToDepth" local variable.
            if (!formations.isEmpty()) {
                previousFormation = formations
                        .get(formations.size() - 1);
                lastToDepth = previousFormation.getToDepth();
            }
        
            // From depth
            formation.setFromDepth(lastToDepth);
            
            // To depth
            temp = line.substring(FM_FROM_DEPTH_BEGIN_INDEX, FM_FROM_DEPTH_END_INDEX).trim();
            if (temp.length() > 0) formation.setToDepth(Double.valueOf(temp));
            
            // Formation code
            formation.setFormationCode(previousFormation.getFormationCode());
        }
        else {
            // From depth
            temp = line.substring(FM_FROM_DEPTH_BEGIN_INDEX, FM_FROM_DEPTH_END_INDEX).trim();
            if (temp.length() > 0) formation.setFromDepth(Double.valueOf(temp));

            // To depth
            try{
                temp = line.substring(FM_TO_DEPTH_BEGIN_INDEX, FM_TO_DEPTH_END_INDEX).trim();
                if (temp.length() > 0) formation.setToDepth(Double.valueOf(temp));
            }
            catch(StringIndexOutOfBoundsException ex) {
                // TODO investigate
            }

            // Formation code
            if (line.length() > FM_TO_DEPTH_END_INDEX) {
                formation.setFormationCode(
                    line.substring(FM_CODE_BEGIN_INDEX, FM_CODE_END_INDEX).trim());
            }
        }
            
        // Add formation to list
        FormationViewModel formationView = new FormationViewModel(formation);
        formations.add(formationView);
    }
    
    private void parseSample(String line, List<Sample> samples) {
        String temp;
        double lastToDepth = 0;
        Sample lastSample = null;
        Sample sample = new Sample();
        String lastComment; 
        
        // Get last sample (if it exists) to get the last "to depth" value and
        // overwrite "lastToDepth" local variable.
        if (!samples.isEmpty()) {
            lastSample = samples
                    .get(samples.size() - 1);
            lastToDepth = lastSample.getToDepth();
        }
        
        // TODO check corrupt file for dade.dat
        if (line.length() < ROCK_TYPE_END_INDEX) return;
        
        // Evaluate rock type field to determine what type of sample record this is
        temp = line.substring(ROCK_TYPE_BEGIN_INDEX, ROCK_TYPE_END_INDEX).trim();
        sample.setRockTypeCode(temp);
        
        if (sample.getRockTypeCode().equals("Z")) {
            if (lastSample != null) {
                lastComment = lastSample.getComments();
                String comment = line.substring(SAMPLE_COMMENT_BEGIN_INDEX).trim().toLowerCase();
                
                if (comment.length() > 0) {
                    if (lastComment == null || lastComment.length() == 0) 
                        lastComment = capitalizeFirstLetter(comment);
                    else
                        lastComment += " " + comment;

                    lastSample.setComments(lastComment);
                }
            }
        } 
        else {
            
            sample.setFromDepth(lastToDepth);
            
            // We have enough information to add the sample to 
            // the well log at this point
            samples.add(sample);
            
            parseSampleDepth(line, samples);
            
            // "N" = no sample record, "V" = same as previous sample record
            // Some records don't have any data after the rock type code.
            // Check for length and return if we don't have any more data.
            if (sample.getRockTypeCode().equals("N") || 
                sample.getRockTypeCode().equals("V")) {
                
                return;
            }
            
            parseRockColors(line, samples);
            parsePorosity(line, samples);
            
            switch (sample.getRockTypeCode()) {
                case "S": // Sand
                    parseSand(line, samples);
                    break;
                case "F": // Shell Bed
                    parseShellBed(line, samples);
                    break;
                case "L": // Limestone
                case "E": // Calcarenite
                case "M": // Calcilutite
                    parseLimestone(line, samples);
                    break;
                case "D": // Dolostone
                    parseDolomite(line, samples);
                    break;
                case "C": // Clay
                    parseClay(line, samples);
                    break;
            }
        }
    }
    
    private void parseSand(String line, List<Sample> samples) {
        parseGrainSize(line, samples);
        parseGrainRange(line, samples);
        parseRoundness(line, samples);
        parseSphericity(line, samples);
        parseInduration(line, samples);
        parseMineralsIntoSample(line, samples);
        parseFeaturesIntoSample(line, samples);
        parseFossilsIntoSample(line, samples);
    }
    
    private void parseShellBed(String line, List<Sample> samples) {
        parseInduration(line, samples);
        parseMineralsIntoSample(line, samples);
        parseFeaturesIntoSample(line, samples);
        parseFossilsIntoSample(line, samples);
    }
    
    private void parseLimestone(String line, List<Sample> samples) {
        parseGrainTypes(line, samples);
        parseLimestoneGrainSize(line, samples);
        parseLimestoneGrainRange(line, samples);
        parseInduration(line, samples);
        parseCementTypesIntoSample(line, samples);
        parseMineralsIntoSample(line, samples);
        parseFeaturesIntoSample(line, samples);
        parseFossilsIntoSample(line, samples);
    }
    
    private void parseDolomite(String line, List<Sample> samples) {
        parseAlterationCode(line, samples);
        parseCrystallinityCode(line, samples);
        parseDolomiteGrainSize(line, samples);
        parseDolomiteGrainRange(line, samples);
        parseInduration(line, samples);
        parseCementTypesIntoSample(line, samples);
        parseMineralsIntoSample(line, samples);
        parseFeaturesIntoSample(line, samples);
        parseFossilsIntoSample(line, samples);
    }
    
    private void parseClay(String line, List<Sample> samples) {
        parseInduration(line, samples);
        parseMineralsIntoSample(line, samples);
        parseFeaturesIntoSample(line, samples);
        parseFossilsIntoSample(line, samples);
    }
    
    private void parseSampleDepth(String line, List<Sample> samples) {
        String temp;
        double fraction = 0;
        
        if (line.length() < SAMPLE_TO_DEPTH_END_INDEX) return;
        
        // To Depth
        temp = line.substring(SAMPLE_TO_DEPTH_BEGIN_INDEX, SAMPLE_TO_DEPTH_END_INDEX).trim();
        if (temp.length() == 0) return;
        
        double depth = 0;
        
        try{
            depth = Double.valueOf(temp);
        }
        catch(Exception e) {
            System.out.println("DatFileRepo.parseSampleDepth: " + temp);
        }
        
        // Fraction of To Depth
        temp = line.substring(SAMPLE_TO_DEPTH_END_INDEX, SAMPLE_TO_DEPTH_END_INDEX + 1).trim();

        if (!temp.equals(""))
            fraction = Double.valueOf(temp) * 0.1;

        samples.get(
            samples.size() - 1).setToDepth(depth + fraction);
    }
    
    private void parseRockColors(String line, List<Sample> samples) {
        String temp;
        
        if (line.length() < ROCK_COLOR_END_INDEX) return;
        
        temp = line.substring(ROCK_COLOR_BEGIN_INDEX, ROCK_COLOR_END_INDEX).trim();
        samples.get(
            samples.size() - 1).setRockColorCodeMin(temp);

        if (line.length() < ROCK_COLOR2_END_INDEX) return;
        
        temp = line.substring(ROCK_COLOR2_BEGIN_INDEX, ROCK_COLOR2_END_INDEX).trim();
        samples.get(
            samples.size() - 1).setRockColorCodeMax(temp);
    }
    
    private void parsePorosity(String line, List<Sample> samples) {
        int endIndex;
        String temp;
        String[] dataArray;
        
        if (line.length() < POROSITY_END_INDEX) return;
        
        temp = line.substring(POROSITY_BEGIN_INDEX, POROSITY_END_INDEX).trim();
        if ("".equals(temp)) temp = "-1";
        
        try {
            samples.get(samples.size() - 1)
                    .setPorosity(Integer.valueOf(temp));
        } catch (NumberFormatException e) {
            System.out.println("DatFileRepo.parsePorosity-NumberFormat: " + temp);
        } catch (Exception e) {
            System.out.println("DatFileRepo.parsePorosity-Other:" + temp);
        }

        // Validate length
        if (line.length() <= POROSITY_CODE_BEGIN_INDEX) return;

        // Establish end index
        endIndex = POROSITY_CODE_END_INDEX;
        if (line.length() > POROSITY_CODE_BEGIN_INDEX &&
            line.length() <= POROSITY_CODE_END_INDEX) {
            endIndex = line.length();
        }

        temp = line.substring(POROSITY_CODE_BEGIN_INDEX, endIndex).trim();
        dataArray = temp.split("");
        
        // Validate code before storing it
        LookupCodes codes = new LookupCodes();
        
        for (String data : dataArray) {
            if (codes.getPorosityCodeMap().containsKey(data)) {
            samples.get(samples.size() - 1)
                .getPorosityCodes().add(data);
            }
        }
    }
    
    private void parseGrainSize(String line, List<Sample> samples) {
        String temp;
        
        if (line.length() < GRAIN_SIZE_END_INDEX) return;
        
        temp = line.substring(GRAIN_SIZE_BEGIN_INDEX, GRAIN_SIZE_END_INDEX).trim();
        
        samples.get(samples.size() - 1)
                .setGrainSizeCode(temp);
    }
    
    private void parseGrainRange(String line, List<Sample> samples) {
        String temp;
        
        if (line.length() < RANGE_MIN_END_INDEX) return;
        
        temp = line.substring(RANGE_MIN_BEGIN_INDEX, RANGE_MIN_END_INDEX).trim();
        
        samples.get(samples.size() - 1)
                .setGrainRangeCodeMin(temp);
        
        if (line.length() < RANGE_MAX_END_INDEX) return;
        
        temp = line.substring(RANGE_MAX_BEGIN_INDEX, RANGE_MAX_END_INDEX).trim();
        samples.get(samples.size() - 1)
                .setGrainRangeCodeMax(temp);
    }
    
    private void parseRoundness(String line, List<Sample> samples) {
        String temp;
        
        if (line.length() < ROUNDNESS_MIN_END_INDEX) return;
        
        temp = line.substring(ROUNDNESS_MIN_BEGIN_INDEX, ROUNDNESS_MIN_END_INDEX).trim();
        
        samples.get(samples.size() - 1)
                .setRoundnessCodeMin(temp);
        
        if (line.length() < ROUNDNESS_MAX_END_INDEX) return;
        
        temp = line.substring(ROUNDNESS_MAX_BEGIN_INDEX, ROUNDNESS_MAX_END_INDEX).trim();
        samples.get(samples.size() - 1)
                .setRoundnessCodeMax(temp);
    }
    
    private void parseSphericity(String line, List<Sample> samples) {
        String temp;
        
        if (line.length() < SPHERICITY_END_INDEX) return;
        
        temp= line.substring(SPHERICITY_BEGIN_INDEX, SPHERICITY_END_INDEX).trim();
        samples.get(samples.size() - 1)
                .setSphericityCode(temp);
    }
    
    private void parseGrainTypes(String line, List<Sample> samples) {
        int endIndex;
        String temp;
        String[] dataArray;

        // Validate length
        if (line.length() <= GRAIN_TYPE_END_INDEX) return;

        // Establish end index
        endIndex = GRAIN_TYPE_END_INDEX;
        if (line.length() > GRAIN_TYPE_BEGIN_INDEX &&
            line.length() <= GRAIN_TYPE_END_INDEX) {
            endIndex = line.length();
        }

        temp = line.substring(GRAIN_TYPE_BEGIN_INDEX, endIndex).trim();
        dataArray = temp.split("");
        samples.get(samples.size() - 1)
                .getGrainTypeCodes().addAll(Arrays.asList(dataArray));
    }
    
    private void parseLimestoneGrainSize(String line, List<Sample> samples) {
        String temp;
        
        if (line.length() < LIMESTONE_GRAIN_SIZE_END_INDEX) return;
        
        temp = line.substring(LIMESTONE_GRAIN_SIZE_BEGIN_INDEX, 
                LIMESTONE_GRAIN_SIZE_END_INDEX).trim();
        
        samples.get(samples.size() - 1)
                .setGrainSizeCode(temp);
    }
    
    private void parseLimestoneGrainRange(String line, List<Sample> samples) {
        String temp;
        
        if (line.length() < LIMESTONE_RANGE_MIN_END_INDEX) return;
        
        temp = line.substring(LIMESTONE_RANGE_MIN_BEGIN_INDEX, 
                LIMESTONE_RANGE_MIN_END_INDEX).trim();
        
        samples.get(samples.size() - 1)
                .setGrainRangeCodeMin(temp);
        
        if (line.length() < LIMESTONE_RANGE_MAX_END_INDEX) return;
        
        temp = line.substring(LIMESTONE_RANGE_MAX_BEGIN_INDEX, 
                LIMESTONE_RANGE_MAX_END_INDEX).trim();
        samples.get(samples.size() - 1)
                .setGrainRangeCodeMax(temp);
    }
    
    private void parseAlterationCode(String line, List<Sample> samples) {
        String temp;
        
        if (line.length() < ALTERATION_END_INDEX) return;
        
        temp = line.substring(ALTERATION_BEGIN_INDEX, 
                ALTERATION_END_INDEX).trim();
        
        samples.get(samples.size() - 1)
                .setAlterationCode(temp);
    }
    
    private void parseCrystallinityCode(String line, List<Sample> samples) {
        String temp;
        
        if (line.length() < CRYSTALLINITY_END_INDEX) return;
        
        temp = line.substring(CRYSTALLINITY_BEGIN_INDEX, 
                CRYSTALLINITY_END_INDEX).trim();
        
        samples.get(samples.size() - 1)
                .setCrystallinityCode(temp);
    }
    
    private void parseDolomiteGrainSize(String line, List<Sample> samples) {
        String temp;
        
        if (line.length() < DOLOMITE_GRAIN_SIZE_END_INDEX) return;
        
        temp = line.substring(DOLOMITE_GRAIN_SIZE_BEGIN_INDEX, 
                DOLOMITE_GRAIN_SIZE_END_INDEX).trim();
        
        samples.get(samples.size() - 1)
                .setGrainSizeCode(temp);
    }
    
    private void parseDolomiteGrainRange(String line, List<Sample> samples) {
        String temp;
        
        if (line.length() < DOLOMITE_RANGE_MIN_END_INDEX) return;
        
        temp = line.substring(DOLOMITE_RANGE_MIN_BEGIN_INDEX, 
                DOLOMITE_RANGE_MIN_END_INDEX).trim();
        
        samples.get(samples.size() - 1)
                .setGrainRangeCodeMin(temp);
        
        if (line.length() < DOLOMITE_RANGE_MAX_END_INDEX) return;
        
        temp = line.substring(DOLOMITE_RANGE_MAX_BEGIN_INDEX, 
                DOLOMITE_RANGE_MAX_END_INDEX).trim();
        samples.get(samples.size() - 1)
                .setGrainRangeCodeMax(temp);
    }
    
    private void parseCementTypesIntoSample(String line, List<Sample> samples) {
        int endIndex;
        String temp;
        String[] dataArray;

        // Validate length
        if (line.length() <= CEMENTS_BEGIN_INDEX) return;

        // Establish end index
        endIndex = CEMENTS_END_INDEX;
        if (line.length() > CEMENTS_BEGIN_INDEX &&
            line.length() <= CEMENTS_END_INDEX) {
            endIndex = line.length();
        }

        temp = line.substring(CEMENTS_BEGIN_INDEX, endIndex).trim();
        dataArray = temp.split("");
        samples.get(samples.size() - 1)
                .getCementTypeCodes().addAll(Arrays.asList(dataArray));
    }
    
    private void parseInduration(String line, List<Sample> samples) {
        String temp;
        
        if (line.length() < INDURATION_END_INDEX) return;

        temp = line.substring(INDURATION_BEGIN_INDEX, INDURATION_END_INDEX).trim();
        samples.get(samples.size() - 1)
                .setIndurationCode(temp);
    }
    
    private void parseSedimentaryIntoSample(String line, List<Sample> samples) {
        int endIndex;
        String temp;
        String[] dataArray;

        // Validate length
        if (line.length() <= SEDIMENTARY_BEGIN_INDEX) return;

        // Establish end index
        endIndex = SEDIMENTARY_END_INDEX;
        if (line.length() > SEDIMENTARY_BEGIN_INDEX &&
            line.length() <= SEDIMENTARY_END_INDEX) {
            endIndex = line.length();
        }

        temp = line.substring(SEDIMENTARY_BEGIN_INDEX, endIndex).trim();
        dataArray = temp.split("");
        samples.get(samples.size() - 1)
                .getSedimentaryCodes().addAll(Arrays.asList(dataArray));
    }
    
    private void parseMineralsIntoSample(String line, List<Sample> samples) {
        int endIndex;
        String temp;
        String[] dataArray;
        
        // If no data exists, return
        if (line.length() <= MINERALS_BEGIN_INDEX) return;

        // Establish the end index. This is a variable length index as it
        // depends on how many mineral codes were recorded for the sample.
        endIndex = MINERALS_END_INDEX;
        if (line.length() > MINERALS_BEGIN_INDEX &&
            line.length() <= MINERALS_END_INDEX) {
            endIndex = line.length();
        }

        temp = line.substring(MINERALS_BEGIN_INDEX, endIndex).trim();

        // If no data exists, return
        if (temp.length() == 0) return;

        // Split string into an array of individual minerals and percentages
        dataArray = parseMineralCodesIntoArray(temp);
        
        for (String value : dataArray) {
            Mineral mineral = new Mineral();

            mineral.setCode(value.substring(0, 1));

            // Check for no data found after the mineral code
            if (value.length() == 1) {
                continue;
            }

            temp = value.substring(1, value.length()).trim();

            try {
                if (temp.length() > 0) {
                    if (temp.contains("T")) {
                        // The value "T" was seen in the bay county file
                        // for well log 17337
                        mineral.setPercentage(1);
                    }
                    else if (temp.contains("<")) {
                        // The value "<" was seen in the charlott county file
                        // for well log 10232. This symbol implies less than
                        // then the next number. For simplification, just
                        // round this number up to the value.
                        temp = temp.substring(1, temp.length()).trim();
                        mineral.setPercentage(Double.valueOf(temp));
                    }
                    else {
                        mineral.setPercentage(Double.valueOf(temp));
                    }
                }

            } catch (NumberFormatException e) {
                System.out.println("DatFileRepo.parseMineralsIntoSample: Sample = " + samples.size());
                System.out.println("DatFileRepo.parseMineralsIntoSample: Code = " + mineral.getCode());
                System.out.println("DatFileRepo.parseMineralsIntoSample:" + temp + " - " + value);
            }

            samples.get(samples.size() - 1)
                    .getAccessoryMineralCodes().add(mineral);
        }
    }
    
    private String[] parseMineralCodesIntoArray(String line) {
        List<String> codes = new ArrayList<>();
        String[] dataArray;
        int index = 0;
        
        while (index < line.length()) {
            codes.add(line.substring(index, Math.min(index + 3,line.length())));
            index=index + 3;
        }
        
        dataArray = new String[codes.size()];
        codes.toArray(dataArray);
        
        return dataArray;
    }
    
    private void parseFeaturesIntoSample(String line, List<Sample> samples) {
        int endIndex;
        String temp;
        String[] dataArray;

        // Validate length
        if (line.length() <= FEATURES_BEGIN_INDEX) return;

        // Establish end index
        endIndex = FEATURES_END_INDEX;
        if (line.length() > FEATURES_BEGIN_INDEX &&
            line.length() <= FEATURES_END_INDEX) {
            endIndex = line.length();
        }

        temp = line.substring(FEATURES_BEGIN_INDEX, endIndex).trim();
        dataArray = temp.split("");
        samples.get(samples.size() - 1)
                .getOtherFeatureCodes().addAll(Arrays.asList(dataArray));
    }
    
    private void parseFossilsIntoSample(String line, List<Sample> samples) {
        String temp;
        String[] dataArray;
        
        if (line.length() <= FOSSILS_INDEX) return;

        temp = line.substring(FOSSILS_INDEX).trim();
        dataArray = temp.split("");
        samples.get(samples.size() - 1)
                .getFossilCodes().addAll(Arrays.asList(dataArray));
    }
            
    private String capitalizeFirstLetter(String original){
        if(original.length() == 0)
            return original;
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }

}

class SortByWellNumber implements Comparator<WellNumberEntity>{
 
    @Override
    public int compare(WellNumberEntity entity1, WellNumberEntity entity2) {
        //return entity1.getWellNumber().compareTo(entity2.getWellNumber());
        return entity1.getWellNumber().compareTo(entity2.getWellNumber());
    }
}