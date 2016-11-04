package app;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.osgi.util.NLS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class M extends NLS {

	public static String AbioticResourceDepletionElements;
	public static String AbioticResourceDepletionFossilFuels;
	public static String AccessRestrictions;
	public static String AccreditedThirdPartyReview;
	public static String Acidification;
	public static String AddAMaterialProperty;
	public static String AddReview;
	public static String AdministrativeInformation;
	public static String Amount;
	public static String Average;

	public static String CalculateResults;
	public static String Category;
	public static String CategoryPath;
	public static String ChangeProduct;
	public static String Classification;
	public static String ClassificationSystem;
	public static String Comment;
	public static String CompleteReviewReport;
	public static String ComponentsForReuse;
	public static String ConnectToSoda4LCA;
	public static String Copyright;
	public static String CreateANewEPD;
	public static String CreateANewEPD_Description;

	public static String DataEntry;
	public static String DataSetInformation;
	public static String DataSetUploaded;
	public static String DataSetUploadedMessage;
	public static String DataSources;
	public static String DataStock;
	public static String DeclaredProduct;
	public static String DeclaredUnit;
	public static String Default;
	public static String DeleteReview;
	public static String DependentInternalReview;
	public static String Description;
	public static String Documentation;
	public static String Documentor;
	public static String EPD;
	public static String EPDEditor;
	public static String EPD_DOWNLOAD_FAILED;
	public static String EnvironmentalIndicators;
	public static String EnvironmentalParameters;
	public static String Eutrophication;
	public static String Export;
	public static String ExportEnergy;
	public static String ExternalDocumentationSources;
	public static String File;
	public static String Flow;
	public static String FlowDiagramsOrPictures;
	public static String FreshWaterNetUse;
	public static String GeneralInformation;
	public static String Generic;
	public static String GenericProduct;
	public static String Geography;
	public static String GeographyDescription;
	public static String GetFromServer;
	public static String GlobalWarming;
	public static String Group;
	public static String HazardousWaste;
	public static String Import;
	public static String ImportClassificationFile;
	public static String IndependentExternalReview;
	public static String IndependentInternalReview;
	public static String IndependentReviewPanel;
	public static String Indicator;
	public static String IndicatorMapping;
	public static String IndicatorMapping_Description;
	public static String Indicator_ABIOTIC_RESOURCE_DEPLETION_ELEMENTS;
	public static String Indicator_ABIOTIC_RESOURCE_DEPLETION_FOSSIL_FUELS;
	public static String Indicator_ACIDIFICATION;
	public static String Indicator_COMPONENTS_REUSE;
	public static String Indicator_EUTROPHICATION;
	public static String Indicator_EXPORTED_ENERGY_ELECTRICAL;
	public static String Indicator_EXPORTED_ENERGY_THERMAL;
	public static String Indicator_GLOBAL_WARMING;
	public static String Indicator_HAZARDOUS_WASTE;
	public static String Indicator_MATERIALS_ENERGY_RECOVERY;
	public static String Indicator_MATERIALS_RECYCLING;
	public static String Indicator_NET_FRESH_WATER;
	public static String Indicator_NON_HAZARDOUS_WASTE;
	public static String Indicator_NON_RENEWABLE_PRIMARY_ENERGY_NON_RAW_MATERIALS;
	public static String Indicator_NON_RENEWABLE_PRIMARY_ENERGY_RAW_MATERIALS;
	public static String Indicator_NON_RENEWABLE_SECONDARY_FUELS;
	public static String Indicator_OZONE_DEPLETION;
	public static String Indicator_PHOTOCHEMICAL_OZONE_CREATION;
	public static String Indicator_RADIOACTIVE_WASTE_DISPOSED;
	public static String Indicator_RENEWABLE_PRIMARY_ENERGY_NON_RAW_MATERIALS;
	public static String Indicator_RENEWABLE_PRIMARY_ENERGY_RAW_MATERIALS;
	public static String Indicator_RENEWABLE_SECONDARY_FUELS;
	public static String Indicator_SECONDARY_MATERIAL;
	public static String Indicator_TOTAL_NON_RENEWABLE_PRIMARY_ENERGY;
	public static String Indicator_TOTAL_RENEWABLE_PRIMARY_ENERGY;
	public static String InvalidName;
	public static String IsVendorSpecific;
	public static String LCAMethodDetails;
	public static String LastUpdate;
	public static String Location;
	public static String MaterialProperties;
	public static String MaterialProperties_Description;
	public static String MaterialsForEnergyRecovery;
	public static String MaterialsForRecycling;
	public static String ModellingAndValidation;
	public static String Module;
	public static String Modules;
	public static String MyEPDs;
	public static String Name;
	public static String NetworkConnectionFailed;
	public static String NewEPD;
	public static String NoSearchPossible;
	public static String NonHazardousWaste;
	public static String None;
	public static String NotANumber;
	public static String NotAProduct;
	public static String NotAProduct_Info;
	public static String NotReviewed;

	public static String OekobaudatWebsite;
	public static String OekobaudatWebsite_Description;
	public static String OnlineSearch;
	public static String OpenEditor;
	public static String OpenInBrowser;
	public static String OutputParameters;
	public static String Owner;
	public static String OzoneDepletion;

	public static String Password;
	public static String PhotochemicalOzoneCreation;
	public static String Pictogram;
	public static String ProcessInformation;
	public static String ProductSystem;
	public static String Property;
	public static String PublicationAndOwnership;
	public static String QuantitativeProperties;
	public static String RadioactiveWasteDisposed;
	public static String ReferenceYear;
	public static String Representative;
	public static String ResourceParameters;
	public static String ResultValueMustBeANumber;
	public static String Results;
	public static String Review;
	public static String ReviewDetails;
	public static String ReviewType;
	public static String Reviewer;
	public static String Reviews;

	public static String SafetyMargin;
	public static String SafetyMargins;
	public static String SaveAs;
	public static String SaveWithNewProduct;
	public static String SaveWithNewProduct_Question;

	public static String Scenario;
	public static String Scenarios;
	public static String Search;
	public static String SearchAndDownloads;
	public static String SearchEPDs;
	public static String SearchEPDs_Description;
	public static String SelectACategory;
	public static String SelectAMaterialProperty;
	public static String SelectAProductSystem;
	public static String ServerConfiguration;
	public static String ServerConfiguration_Description;
	public static String Source;
	public static String Sources;
	public static String Specific;
	public static String StartPage;
	public static String Subtype;
	public static String SynchronizeWithModules;
	public static String Synonyms;
	public static String TechnologicalApplicability;
	public static String Technology;
	public static String TechnologyDescription;
	public static String Template;
	public static String TestConnection;
	public static String Time;
	public static String TimeDescription;
	public static String UUID;
	public static String Unit;
	public static String UnitDescription;
	public static String UpdateMajorVersion;
	public static String UpdateMinorVersion;
	public static String UploadDataSet;
	public static String UploadDataSet_Question;
	public static String Uploads;
	public static String UseAdvice;
	public static String User;
	public static String ValidUntil;
	public static String Value;
	public static String Vendor;
	public static String VendorInformation;
	public static String Version;
	public static String WasteParameters;

	private static Map<String, String> map;

	static {
		NLS.initializeMessages("app.messages", M.class);
	}

	private M() {
	}

	public static Map<String, String> getMap() {
		if (map == null)
			map = new HashMap<>();
		try {
			for (Field field : M.class.getDeclaredFields()) {
				if (!Objects.equals(field.getType(), String.class))
					continue;
				if (!Modifier.isStatic(field.getModifiers()))
					continue;
				if (!Modifier.isPublic(field.getModifiers()))
					continue;
				String val = (String) field.get(null);
				map.put(field.getName(), val);
			}
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(M.class);
			log.error("failed to get messages as map", e);
		}
		return map;
	}

	public static String asJson() {
		try {
			Gson gson = new Gson();
			return gson.toJson(getMap());
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(M.class);
			log.error("failed to get messages as JSON string", e);
			return "{}";
		}
	}
}
