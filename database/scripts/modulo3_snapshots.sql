-- Módulo 3 (Gestión del almacenamiento): historial de tamaño para medir crecimiento.
USE DB_ProyectoGrupo4;
GO

IF OBJECT_ID('dbo.AlmacenamientoSnapshot') IS NULL
BEGIN
CREATE TABLE dbo.AlmacenamientoSnapshot (
                                            SnapshotId     INT IDENTITY(1,1) PRIMARY KEY,
                                            FechaCaptura   DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
                                            BaseDatos      SYSNAME       NOT NULL,
                                            TamanoDatosMB  DECIMAL(18,2) NOT NULL,
                                            TamanoLogMB    DECIMAL(18,2) NOT NULL
);

CREATE INDEX IX_AlmacenamientoSnapshot_Base_Fecha
    ON dbo.AlmacenamientoSnapshot (BaseDatos, FechaCaptura);
END
GO