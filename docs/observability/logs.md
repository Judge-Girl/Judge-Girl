# Logs

Log is essential to tracing and trouble-shooting.

In Judge Girl, we write logs using SLF4J and Logback with a strict logging format for the future analyzing.

## Format

See `Spring-Boot/Spring-Boot-Commons/src/main/resources/logback-spring.xml` for the details.

In the message part of a log, the format must be:
An iconic header followed by a list of key-value pairs that represent an object's content, along with some descriptive
words that help read the log. The log should be arranged with proper spaces.

`[<HEADER>] <...(<key-value-pairs>|<some descriptive words>|<proper spaces>)>`

- The `<HEADER>` can be:
    - An url, destination, or an endpoint. (For `TRACE` and `DEBUG`)
    - A command's name. (For `INFO`)
    - The status of an operation. (FOR `DEBUG` and `WARN`, e.g., Sign Up Successfully, Not Found, Forbidden Access)
    - A system error's name. (FOR `ERROR`, e.g., API Timeout)

`<key-value-pairs> = ...<key>=<val>` - you log the content of an object in a key-value form. If the `<val>` may contain
spaces, enclose it with a double quotation marks (`"`).

`<some descriptive words>`- Can be any words you think it helps read the logs.

- The followings are examples of a message:
    - `[Sign Up Successfully] id=51 name="adminName" email="admin@example.com"`
    - `[Produce: LiveSubmissionEvent] problemId=1 languageEnvName=C studentId=12345 submissionId={string=h, int=1, long=1} submissionTime=1623301706487, with bag: string="h" int="1" long="1"`

## Level

TRACE
===

Verbose in-process or service-level traces.

- For example:
    - The path of the incoming RESTFul requests and the status of its response.
    - The status of the Message Queue's producer and consumer.
    - Detailed series of operations of some important usecases.

DEBUG
===

1. **Verbose data** provided **only for** trouble-shooting
2. **Not-Found** exception.

- For example:
    - The verbose content of a request or message queue's message.

INFO
===

All **commands** (NOT QUERY). <br>
All event messages that provide insight which can be analyzed.

The commands must include important attributes (in the key-value pair).

- For example:
    - SignIn, SubmitCode, AnswerQuestion, PatchProblem, and so on.
    
WARN
===

1. Non-crucial system errors that represents **expected unavailability** in Judge Girl.
2. **Suspicious** operations.
3. Illegal operations / Forbidden access

- For example:
    - An operation results in a failure, but it's caught and can be handled by the client.
        - e.g., A Judger that cannot be deployed, so we will retry later.
        - A http request to a downstream service fails, but it's expected and can be handled.
          (maybe we don't expect the service is always online)
    - A request that contains a spam, or a long content (especially binary content).

ERROR
===

Crucial **system errors** that represent certain **unexpected unavailability** of Judge GIrl.

- For example:
    - An HTTP request to a down-stream service results in a failure, it is in a crucial path and cannot be handled by
      the client.
    - Many Judger deployments fail, and the failures continues on every retry. This is a crucial error.
    - An operation that must succeed, but it fails due to the unavailability of other components.
        
