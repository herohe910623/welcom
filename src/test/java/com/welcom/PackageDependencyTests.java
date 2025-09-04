package com.welcom;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packagesOf = WelcomApplication.class)
public class PackageDependencyTests {

    private static final String ACCOUNT = "..modules.account..";
    private static final String COMPANY = "..modules.company..";
    private static final String EVENT = "..modules.event..";
    private static final String TAG = "..modules.tag..";
    private static final String ZONE = "..modules.zone..";
    private static final String MAIN = "..modules.main..";


    @ArchTest
    ArchRule modulesPackageRule = classes().that().resideInAPackage("com.welcom.modules..")
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage("com.welcom.modules..");

    @ArchTest
    ArchRule companyPackageRule = classes().that().resideInAPackage(COMPANY)
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage(COMPANY, EVENT, MAIN);

    @ArchTest
    ArchRule eventPackageRule = classes().that().resideInAPackage(EVENT)
            .should().accessClassesThat().resideInAnyPackage(COMPANY, ACCOUNT, EVENT);

    @ArchTest
    ArchRule accountPackageRule = classes().that().resideInAPackage(ACCOUNT)
            .should().accessClassesThat().resideInAnyPackage(TAG, ZONE, ACCOUNT);

    //package 들을 조각내서 순환참조
    @ArchTest
    ArchRule cycleCheck = slices().matching("com.welcom.(*)..")
            .should().beFreeOfCycles();
}
