package app;

import org.eclipse.osgi.util.NLS;

public class Tooltips extends NLS {

	// All
	public static String All_GeneralInformation;
	public static String All_UUID;
	public static String All_File;
	public static String All_Classification;
	public static String All_AdministrativeInformation;
	public static String All_LastUpdate;
	public static String All_DataSetVersion;

	// EPD
	public static String EPD_Name;
	public static String EPD_FurtherProperties;
	public static String EPD_Synonyms;
	public static String EPD_Comment;
	public static String EPD_DeclaredProduct;
	public static String EPD_ProductAmount;
	public static String EPD_ProductUnit;
	public static String EPD_ExternalDocumentationSources;
	public static String EPD_UncertaintyMargins;
	public static String EPD_UncertaintyMarginsDescription;
	public static String EPD_Time;
	public static String EPD_ReferenceYear;
	public static String EPD_ValidUntil;
	public static String EPD_TimeDescription;
	public static String EPD_Geography;
	public static String EPD_Location;
	public static String EPD_GeographyDescription;
	public static String EPD_Technology;
	public static String EPD_TechnologyDescription;
	public static String EPD_TechnicalPrupose;
	public static String EPD_Pictogram;
	public static String EPD_FlowDiagramsOrPictures;
	public static String EPD_ModellingAndValidation;
	public static String EPD_Subtype;
	public static String EPD_UseAdvice;
	public static String EPD_LCAMethodDetails;
	public static String EPD_DataSources;
	public static String EPD_ComplianceDeclarations;
	public static String EPD_Review;
	public static String EPD_ReviewType;
	public static String EPD_ReviewDetails;
	public static String EPD_ReviewReport;
	public static String EPD_Reviewer;
	public static String EPD_DataEntry;
	public static String EPD_Documentor;
	public static String EPD_DataFormats;
	public static String EPD_PublicationAndOwnership;
	public static String EPD_Owner;
	public static String EPD_Copyright;
	public static String EPD_AccessRestrictions;
	public static String EPD_EPDProfile;
	public static String EPD_Scenarios;
	public static String EPD_Modules;
	public static String EPD_Results;

	// Flow
	public static String Flow_Name;
	public static String Flow_Synonyms;
	public static String Flow_Description;
	public static String Flow_GenericProduct;
	public static String Flow_VendorInformation;
	public static String Flow_IsVendorSpecific;
	public static String Flow_Vendor;
	public static String Flow_VendorDocumentation;
	public static String Flow_FlowProperties;
	public static String Flow_MaterialProperties;

	// Contact
	public static String Contact_ShortName;
	public static String Contact_Name;
	public static String Contact_Address;
	public static String Contact_Telephone;
	public static String Contact_Telefax;
	public static String Contact_Website;
	public static String Contact_Logo;

	// Source
	public static String Source_ShortName;
	public static String Source_Citation;
	public static String Source_Description;
	public static String Source_Logo;
	public static String Source_BelongsTo;
	public static String Source_LinksToExternalFiles;

	// FlowProperty
	public static String FlowProperty_Name;
	public static String FlowProperty_Synonyms;
	public static String FlowProperty_Description;
	public static String FlowProperty_UnitGroup;

	// UnitGroup
	public static String UnitGroup_Name;
	public static String UnitGroup_Description;
	public static String UnitGroup_Units;

	// LCIAMethod
	public static String LCIAMethod_Name;
	public static String LCIAMethod_Methodologies;
	public static String LCIAMethod_ImpactCategories;
	public static String LCIAMethod_ImpactIndicator;
	public static String LCIAMethod_Description;

	static {
		NLS.initializeMessages("app.tooltips", Tooltips.class);
	}

	private Tooltips() {
	}

}
