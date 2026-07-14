import {
  useEffect,
  useRef,
} from "react";

import { Html5Qrcode } from "html5-qrcode";
import { toast } from "react-hot-toast";

interface QrScannerProps {
  onScan: (value: string) => void;
  onClose: () => void;
}

export default function QrScanner({
  onScan,
  onClose,
}: QrScannerProps) {
  const scannerRef =
    useRef<Html5Qrcode | null>(null);

  const readerId = "qr-reader";

  useEffect(() => {
    let mounted = true;

    const startScanner = async () => {
      try {
        const scanner =
          new Html5Qrcode(readerId);

        scannerRef.current = scanner;

        await scanner.start(
          {
            facingMode: "environment",
          },
          {
            fps: 10,
            qrbox: {
              width: 250,
              height: 250,
            },
          },
          async (decodedText) => {
            if (!mounted) {
              return;
            }

            await scanner.stop();
            scanner.clear();

            onScan(decodedText);
          },
          () => {}
        );
      } catch {
        toast.error(
          "Unable to access the camera."
        );

        onClose();
      }
    };

    void startScanner();

    return () => {
      mounted = false;

      const scanner =
        scannerRef.current;

      if (scanner) {
  void scanner
    .stop()
    .catch(() => {})
    .finally(() => {
      scanner.clear();
    });
}
    };
  }, [onScan, onClose]);

  return (
    <div
      id={readerId}
      className="w-full"
    />
  );
}