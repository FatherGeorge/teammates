package teammates.test.cases.ui.browsertests;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import teammates.common.datatransfer.DataBundle;
import teammates.common.datatransfer.StudentAttributes;
import teammates.common.util.AppUrl;
import teammates.common.util.Const;
import teammates.common.util.StringHelper;
import teammates.common.util.ThreadHelper;
import teammates.test.driver.BackDoor;
import teammates.test.driver.EmailAccount;
import teammates.test.driver.TestProperties;
import teammates.test.pageobjects.Browser;
import teammates.test.pageobjects.BrowserPool;
import teammates.test.pageobjects.InstructorCourseDetailsPage;
import teammates.test.pageobjects.InstructorCourseStudentDetailsEditPage;
import teammates.test.pageobjects.InstructorCourseStudentDetailsViewPage;
import teammates.test.pageobjects.InstructorStudentRecordsPage;

/**
 * Tests 'Course Details' view for Instructors.
 * SUT {@link InstructorCourseDetailsPage}. <br>
 * This class uses real user accounts for students.
 */
public class InstructorCourseDetailsPageUiTest extends BaseUiTestCase {
    private static Browser browser;
    private static InstructorCourseDetailsPage detailsPage;
    private static DataBundle testData;
    
    private static String instructorId;
    private static String courseId;
    
    private static StudentAttributes alice;
    private static StudentAttributes charlie;
    
    @BeforeClass
    public static void classSetup() throws Exception {
        printTestClassHeader();
        testData = loadDataBundle("/InstructorCourseDetailsPageUiTest.json");
        
        // use both the student accounts injected for this test
        
        String aliceGoogleId = TestProperties.inst().TEST_STUDENT1_ACCOUNT;
        String aliceEmail = aliceGoogleId + "@gmail.com";
        String charlieGoogleId = TestProperties.inst().TEST_STUDENT2_ACCOUNT;
        String charlieEmail = charlieGoogleId + "@gmail.com";
        testData.accounts.get("Alice").googleId = aliceGoogleId;
        testData.accounts.get("Charlie").googleId = charlieGoogleId;
        testData.students.get("CCDetailsUiT.alice.tmms@CCDetailsUiT.CS2104").googleId = aliceGoogleId;
        testData.students.get("CCDetailsUiT.alice.tmms@CCDetailsUiT.CS2104").email = aliceEmail;
        testData.students.get("charlie.tmms@CCDetailsUiT.CS2104").email = charlieEmail;
        testData.students.get("CCDetailsUiT.alice.tmms@CCDetailsUiT.CS2103").googleId = aliceGoogleId;
        testData.students.get("CCDetailsUiT.alice.tmms@CCDetailsUiT.CS2103").email = aliceEmail;
        testData.students.get("charlie.tmms@CCDetailsUiT.CS2103").email = charlieEmail;
        
        removeAndRestoreTestDataOnServer(testData);
        browser = BrowserPool.getBrowser(true);
        alice = testData.students.get("CCDetailsUiT.alice.tmms@CCDetailsUiT.CS2104");
        charlie = testData.students.get("charlie.tmms@CCDetailsUiT.CS2104");
    }
    
    @Test 
    public void allTests() throws Exception {
        testContent();
        testCommentToWholeCourse();
        testTableSort();
        //No input validation required
        testLinks();
        testRemindAction();
        testDeleteAction();
    }

    public void testContent() throws Exception {
        
        ______TS("content: no students");
        
        instructorId = testData.instructors.get("CCDetailsUiT.instrForEmptyCourse").googleId;
        courseId = testData.courses.get("CCDetailsUiT.CourseWithoutStudents").id;
        detailsPage = getCourseDetailsPage();
        detailsPage.verifyIsCorrectPage(courseId);

        // This is the full HTML verification for Instructor Course Details Page, the rest can all be verifyMainHtml
        detailsPage.verifyHtml("/InstructorCourseDetailsEmptyCourse.html");
        
        ______TS("content: multiple students with sections");
        
        instructorId = testData.instructors.get("CCDetailsUiT.instr2").googleId;
        courseId = testData.courses.get("CCDetailsUiT.CS2103").id;

        detailsPage = getCourseDetailsPage();
        detailsPage.verifyHtmlMainContent("/InstructorCourseDetailsWithSections.html");
        
        ______TS("content: multiple students with sections with helper view");
        
        instructorId = testData.instructors.get("CCDetailsUiT.instr2Helper").googleId;

        detailsPage = getCourseDetailsPage();
        detailsPage.verifyHtmlMainContent("/InstructorCourseDetailsWithSectionsWithHelperView.html");
        
        ______TS("content: multiple students without sections");
        
        instructorId = testData.instructors.get("CCDetailsUiT.instr").googleId;
        courseId = testData.courses.get("CCDetailsUiT.CS2104").id;
        
        detailsPage = getCourseDetailsPage();
        detailsPage.verifyHtmlMainContent("/InstructorCourseDetailsWithoutSections.html");
    }
    
    private void testCommentToWholeCourse() {
        ______TS("comment to whole course: submit empty comment");
        detailsPage.submitCommentToCourse("");
        detailsPage.verifyStatus("Please enter a valid comment. The comment can't be empty.");
        detailsPage.clickAddCommentToCourseButton();
        
        ______TS("comment to whole course: any comment");
        detailsPage.submitCommentToCourse("this is a comment");
        detailsPage.verifyStatus(Const.StatusMessages.COMMENT_ADDED);
    }

    private void testTableSort() {
        ______TS("content: sorting");
        
        //the first table is the hidden table used for comments' visibility options
        String patternString = "Joined{*}Joined{*}Yet to join{*}Yet to join";
        detailsPage.sortByStatus().verifyTablePattern(1, 4, patternString);
        patternString = "Yet to join{*}Yet to join{*}Joined{*}Joined";
        detailsPage.sortByStatus().verifyTablePattern(1, 4, patternString);
        
        patternString = "Alice Betsy{*}Benny Charles{*}Charlie Davis{*}Danny Engrid";
        detailsPage.sortByName().verifyTablePattern(1, 3, patternString);
        patternString = "Danny Engrid{*}Charlie Davis{*}Benny Charles{*}Alice Betsy";
        detailsPage.sortByName().verifyTablePattern(1, 3, patternString);
        
        patternString = "Team 1{*}Team 1{*}Team 2{*}Team 2";
        detailsPage.sortByTeam().verifyTablePattern(1, 2, patternString);
        patternString = "Team 2{*}Team 2{*}Team 1{*}Team 1";
        detailsPage.sortByTeam().verifyTablePattern(1, 2, patternString);
    }
    
    public void testLinks() {
        
        ______TS("link: view");
        
        InstructorCourseStudentDetailsViewPage studentDetailsPage = detailsPage.clickViewStudent(alice.name);
        studentDetailsPage.verifyIsCorrectPage(alice.email);
        studentDetailsPage.closeCurrentWindowAndSwitchToParentWindow();
        
        ______TS("link: edit");
        
        InstructorCourseStudentDetailsEditPage studentEditPage = detailsPage.clickEditStudent(charlie.name);
        studentEditPage.verifyIsCorrectPage(charlie.email);
        studentEditPage.closeCurrentWindowAndSwitchToParentWindow();
        
        ______TS("link: all records");
        
        InstructorStudentRecordsPage studentAllRecordsPage = detailsPage.clickAllRecordsLink(charlie.name);
        studentAllRecordsPage.verifyIsCorrectPage(charlie.email);
        studentAllRecordsPage.closeCurrentWindowAndSwitchToParentWindow();
        
        studentAllRecordsPage = detailsPage.clickAllRecordsLink(alice.name);
        studentAllRecordsPage.verifyIsCorrectPage(alice.email);
        studentAllRecordsPage.closeCurrentWindowAndSwitchToParentWindow();
        
        ______TS("link: add comment");
        
        InstructorCourseStudentDetailsViewPage studentCommentsPage = detailsPage.clickAddCommentStudent(alice.name);
        studentCommentsPage.verifyIsCorrectPage(alice.email);
        studentCommentsPage.closeCurrentWindowAndSwitchToParentWindow();
        
        ______TS("link: download student list");
        
        AppUrl studentListDownloadUrl = createUrl(Const.ActionURIs.INSTRUCTOR_COURSE_STUDENT_LIST_DOWNLOAD)
                                        .withUserId("CCDetailsUiT.instr")
                                        .withCourseId("CCDetailsUiT.CS2104");
        
        detailsPage.verifyDownloadLink(studentListDownloadUrl);
    }

    public void testRemindAction() throws Exception {

        //Charlie is yet to register
        String charlieEmail = charlie.email;
        String charliePassword = TestProperties.inst().TEST_STUDENT2_PASSWORD;
        
        //Alice is already registered
        String alicePassword = TestProperties.inst().TEST_STUDENT1_PASSWORD;
        
        String courseId = testData.courses.get("CCDetailsUiT.CS2104").id;
        boolean isEmailEnabled = !TestProperties.inst().isDevServer();

        ______TS("action: remind single student");

        detailsPage.clickRemindStudentAndCancel(charlie.name);
        if (isEmailEnabled) {
            assertFalse(didStudentReceiveReminder(courseId, charlieEmail, charliePassword));
        }
        

        detailsPage.clickRemindStudentAndConfirm(charlie.name);
        if (isEmailEnabled) {
            assertTrue(didStudentReceiveReminder(courseId, charlie.email, charliePassword));
        }
        
        // Hiding of the 'Send invite' link is already covered by content test.
        //  (i.e., they contain cases of both hidden and visible 'Send invite' links.

        ______TS("action: remind all");

        detailsPage.clickRemindAllAndCancel();
        detailsPage.clickRemindAllAndConfirm();
        
        if (isEmailEnabled) {
            // verify an unregistered student received reminder
            assertTrue(didStudentReceiveReminder(courseId, charlie.email, charliePassword));
            // verify a registered student did not receive a reminder
            assertFalse(didStudentReceiveReminder(courseId, alice.email, alicePassword));
        }
    }

    public void testDeleteAction() throws Exception {
        
        String courseId = testData.courses.get("CCDetailsUiT.CS2104").id;        
        StudentAttributes benny = testData.students.get("benny.tmms@CCDetailsUiT.CS2104");
        StudentAttributes danny = testData.students.get("danny.tmms@CCDetailsUiT.CS2104");
        
        ______TS("action: delete");
                
        detailsPage.clickDeleteAndCancel(benny.name);
        assertNotNull(BackDoor.getStudent(courseId, benny.email));

        //Use ${test.student1} etc. 
        detailsPage.clickDeleteAndConfirm(benny.name)
                        .verifyHtmlMainContent("/instructorCourseDetailsStudentDeleteSuccessful.html");
                
        detailsPage.clickDeleteAndCancel(danny.name);
        assertNotNull(BackDoor.getStudent(courseId, danny.email));        
    }
    
    private InstructorCourseDetailsPage getCourseDetailsPage() {
        AppUrl detailsPageUrl = createUrl(Const.ActionURIs.INSTRUCTOR_COURSE_DETAILS_PAGE)
                                .withUserId(instructorId)
                                .withCourseId(courseId);

        return loginAdminToPage(browser, detailsPageUrl, InstructorCourseDetailsPage.class);
    }
    
    private boolean didStudentReceiveReminder(String courseId, String studentEmail, String studentPassword) 
                                            throws Exception {
        String keyToSend = StringHelper.encrypt(BackDoor.getKeyForStudent(courseId, studentEmail));
    
        ThreadHelper.waitFor(5000); //TODO: replace this with a more efficient check
        String keyReceivedInEmail = EmailAccount.getRegistrationKeyFromGmail(studentEmail, studentPassword, courseId);
        return (keyToSend.equals(keyReceivedInEmail));
    }

    @AfterClass
    public static void classTearDown() throws Exception {
        BackDoor.removeDataBundleFromDb(testData);
        BrowserPool.release(browser);
    }
}