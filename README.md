# Marvin - A Depressed SlackBot [![](https://circleci.com/gh/neo/marvin-bot.svg?style=shield)](https://circleci.com/gh/neo/marvin-bot)

## Database migrations

This project uses [Flyway] for migrations. The decision has been made to keep the PostgreSQL sequence generation instead
of the global one sequence to rule them all of Hibernate.

The ability for Hibernate to automagically update the database for you has been disabled. Any changes to the database
schema has to be done manually, and a migration made.

To make a new migration create a new entity. The convention for tables and columns matches those of Ruby on Rails, not
the default Hibernate/JPA of just naming it exactly after the class.

An example entity with a custom set table name and a PostgreSQL table sequence.

```java
@Entity
@Table(name = "commands")
public class Command {

    @Id
    // This line creates a sequence `commands_id_seq` that is for the field `id` in the table `commands`.
    @SequenceGenerator(name = "pk_sequence", sequenceName = "commands_id_seq")
    // Wire the sequence up to the id.
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pk_sequence")
    private long id;
    @Column(unique = true)
    private String name;
    private String endpoint;

    private RequestMethod method;
}
```

In development mode any new entity will automatically generate a new table with the correct fields on it. You can then
copy the table definition to a new migration.

**Note:** Tests are run in their own profile and they rely on all database changes to happen through a migration.

The migration file for the above entity is named `V1__Add_commands_table.sql` and contained at the time of writing:

```sql
CREATE TABLE commands
(
    id BIGSERIAL,  -- This is PostgreSQL syntax for a bigint primary key with its own serial sequence.
    endpoint VARCHAR(255),
    method INTEGER,
    name VARCHAR(255)
);
CREATE UNIQUE INDEX uk_py1b1n9yenhsrtlh4gj3p7b4g ON commands (name);
```

## Running migrations

[Flyway] will automatically migrate the database to the latest version on boot.

[Flyway]: https://flywaydb.org/

## Build failing from missing accessors?

This is probably because IntelliJ hasn't been configured to support annotation processors.

Fix it with:

1. Opening the settings window for "Annotation processors"
2. Click "Enable annotation processors"
3. Recompile the entire project

<sup>Footnote: When CircleCi is building, this will show the status of the last completed build.</sup>
