USE DB_ProyectoGrupo4;
GO

IF OBJECT_ID('dbo.PruebaAlmacenamiento') IS NULL
BEGIN
CREATE TABLE dbo.PruebaAlmacenamiento (
                                          Id            INT IDENTITY(1,1) NOT NULL,
                                          Codigo        UNIQUEIDENTIFIER  NOT NULL DEFAULT NEWID(),
                                          Descripcion   VARCHAR(400)      NOT NULL,
                                          FechaRegistro DATETIME2         NOT NULL DEFAULT SYSDATETIME(),
                                          CONSTRAINT PK_PruebaAlmacenamiento PRIMARY KEY CLUSTERED (Id)
) ON FG_DATOS;

CREATE NONCLUSTERED INDEX IX_PruebaAlmacenamiento_Codigo
        ON dbo.PruebaAlmacenamiento (Codigo) ON FG_DATOS;
END
GO

INSERT INTO dbo.PruebaAlmacenamiento (Descripcion)
SELECT TOP (20000)
           REPLICATE('Registro de prueba de almacenamiento ', 8)
       + CAST(ROW_NUMBER() OVER (ORDER BY (SELECT NULL)) AS VARCHAR(10))
FROM sys.all_objects AS a
         CROSS JOIN sys.all_objects AS b;
GO

INSERT INTO dbo.AlmacenamientoSnapshot
    (BaseDatos, TamanoDatosMB, TamanoLogMB, UsadoDatosMB, UsadoLogMB)
SELECT DB_NAME(),
       CAST(SUM(CASE WHEN type = 0 THEN CAST(size AS BIGINT) ELSE 0 END) * 8 / 1024.0 AS DECIMAL(18,2)),
       CAST(SUM(CASE WHEN type = 1 THEN CAST(size AS BIGINT) ELSE 0 END) * 8 / 1024.0 AS DECIMAL(18,2)),
       CAST(SUM(CASE WHEN type = 0 THEN CAST(FILEPROPERTY(name, 'SpaceUsed') AS BIGINT) ELSE 0 END) * 8 / 1024.0 AS DECIMAL(18,2)),
       CAST(SUM(CASE WHEN type = 1 THEN CAST(FILEPROPERTY(name, 'SpaceUsed') AS BIGINT) ELSE 0 END) * 8 / 1024.0 AS DECIMAL(18,2))
FROM sys.database_files;
GO