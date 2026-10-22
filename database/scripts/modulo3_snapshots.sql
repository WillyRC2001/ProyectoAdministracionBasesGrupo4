USE DB_ProyectoGrupo4;
GO

IF OBJECT_ID('dbo.AlmacenamientoSnapshot') IS NULL
BEGIN
CREATE TABLE dbo.AlmacenamientoSnapshot (
                                            SnapshotId     INT IDENTITY(1,1) PRIMARY KEY,
                                            FechaCaptura   DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
                                            BaseDatos      SYSNAME       NOT NULL,
                                            TamanoDatosMB  DECIMAL(18,2) NOT NULL,  -- espacio asignado a los archivos de datos
                                            TamanoLogMB    DECIMAL(18,2) NOT NULL,  -- espacio asignado a los archivos de log
                                            UsadoDatosMB   DECIMAL(18,2) NULL,      -- espacio realmente usado en datos
                                            UsadoLogMB     DECIMAL(18,2) NULL       -- espacio realmente usado en log
);

CREATE INDEX IX_AlmacenamientoSnapshot_Base_Fecha
    ON dbo.AlmacenamientoSnapshot (BaseDatos, FechaCaptura);
END
GO

IF COL_LENGTH('dbo.AlmacenamientoSnapshot', 'UsadoDatosMB') IS NULL
ALTER TABLE dbo.AlmacenamientoSnapshot ADD UsadoDatosMB DECIMAL(18,2) NULL;
GO

IF COL_LENGTH('dbo.AlmacenamientoSnapshot', 'UsadoLogMB') IS NULL
ALTER TABLE dbo.AlmacenamientoSnapshot ADD UsadoLogMB DECIMAL(18,2) NULL;
GO