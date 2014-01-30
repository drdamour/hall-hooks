//Test Fx Imports
import org.specs2.mock.Mockito
import org.specs2.mutable._
import org.specs2.runner._
import org.specs2.time._
import org.junit.runner._

//3rd Party Imports
import play.api.libs.json.Json


//Our stuff imports
import models.Travis._


@RunWith(classOf[JUnitRunner])
class TravisSpec extends Specification with Mockito  {

  "Repository JSON" should {

    val exampleJson =
      """
        |{
        |  "id": 1,
        |  "name": "minimal",
        |  "owner_name": "svenfuchs",
        |  "url": "http://github.com/svenfuchs/minimal"
        |}
      """.stripMargin

    "be parsed to case classes" in {
      val json = Json.parse(exampleJson)

      val o = Json.fromJson[Repository](json).get

      val expected = Repository(1, "minimal", "svenfuchs", "http://github.com/svenfuchs/minimal")
      o must beEqualTo(expected)


    }
  }

  "Payload JSON" should {

    val exampleJson =
      """
        |{
        |    "id": 1,
        |    "number": 1,
        |    "status": null,
        |    "started_at": null,
        |    "finished_at": null,
        |    "status_message": "Passed",
        |    "commit": "62aae5f70ceee39123ef",
        |    "branch": "master",
        |    "message": "the commit message",
        |    "compare_url": "https://github.com/svenfuchs/minimal/compare/master...develop",
        |    "committed_at": "2011-11-11T11: 11: 11Z",
        |    "committer_name": "Sven Fuchs",
        |    "committer_email": "svenfuchs@artweb-design.de",
        |    "author_name": "Sven Fuchs",
        |    "author_email": "svenfuchs@artweb-design.de",
        |    "repository": {
        |       "id": 1,
        |       "name": "minimal",
        |       "owner_name": "svenfuchs",
        |       "url": "http://github.com/svenfuchs/minimal"
        |    }
        |}
      """.stripMargin

    "be parsed to case classes" in {
      val json = Json.parse(exampleJson)

      //Note you must specify the type you're trying to suck out, as it's ambiguous what format to use
      val o = Json.fromJson[BuildMessage](json).get

      val expected = BuildMessage(
        1,
        1,
        None,
        None,
        None,
        "Passed",
        "62aae5f70ceee39123ef",
        "master",
        "the commit message",
        "https://github.com/svenfuchs/minimal/compare/master...develop",
        "2011-11-11T11: 11: 11Z",
        "Sven Fuchs",
        "svenfuchs@artweb-design.de",
        "Sven Fuchs",
        "svenfuchs@artweb-design.de",
        Repository(1, "minimal", "svenfuchs", "http://github.com/svenfuchs/minimal")
      )

      o must beEqualTo(expected)
    }


  }


}

