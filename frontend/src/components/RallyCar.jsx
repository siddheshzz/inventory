import { useEffect, useRef, useState } from 'react';

// Procedural low-poly rally car: real geometry, lighting, shadows and perspective.
export default function RallyCar() {
  const host = useRef(null);
  const [paused, setPaused] = useState(() => window.matchMedia('(prefers-reduced-motion: reduce)').matches);
  const pause = useRef(paused);
  const [failed, setFailed] = useState(false);
  useEffect(() => { pause.current = paused; }, [paused]);
  useEffect(() => {
    let dispose, cancelled = false;
    import('three').then(T => {
      if (cancelled) return;
      const el = host.current;
      let renderer;
      try { renderer = new T.WebGLRenderer({ antialias: true, alpha: true }); }
      catch { setFailed(true); return; }
      renderer.setPixelRatio(Math.min(window.devicePixelRatio, 1.7));
      renderer.shadowMap.enabled = true;
      renderer.shadowMap.type = T.PCFShadowMap;
      el.appendChild(renderer.domElement);
      const scene = new T.Scene();
      const camera = new T.PerspectiveCamera(36, 1, 0.1, 100);
      camera.position.set(6.6, 4, 7.5); camera.lookAt(0, 0.45, 0);
      scene.add(new T.HemisphereLight(0xffffff, 0x49516b, 3));
      const sun = new T.DirectionalLight(0xffffff, 5); sun.position.set(4, 8, 4); sun.castShadow = true; sun.shadow.mapSize.set(1024, 1024); scene.add(sun);
      const rim = new T.DirectionalLight(0xc9ff4c, 3); rim.position.set(-4, 3, -3); scene.add(rim);
      const car = new T.Group(); scene.add(car);
      const materials = {
        body: new T.MeshStandardMaterial({ color: 0xe0fb53, roughness: 0.33, metalness: 0.45 }),
        dark: new T.MeshStandardMaterial({ color: 0x121923, roughness: 0.55 }),
        glass: new T.MeshStandardMaterial({ color: 0x213e52, metalness: 0.85, roughness: 0.15 }),
        white: new T.MeshStandardMaterial({ color: 0xf5f7ee }),
        rim: new T.MeshStandardMaterial({ color: 0xc9cbd1, metalness: 0.9, roughness: 0.3 }),
        light: new T.MeshStandardMaterial({ color: 0xffffe0, emissive: 0xffffb2, emissiveIntensity: 1.2 }),
        red: new T.MeshStandardMaterial({ color: 0xee4433, emissive: 0x992211 }),
      };
      function box(w, h, d, x, y, z, material, parent = car) {
        const mesh = new T.Mesh(new T.BoxGeometry(w, h, d), materials[material]);
        mesh.position.set(x, y, z); mesh.castShadow = true; parent.add(mesh); return mesh;
      }
      box(3.9, 0.57, 1.72, 0, 0.65, 0, 'body');
      box(4, 0.16, 1.8, 0, 0.35, 0, 'dark');
      const cabin = box(1.8, 0.64, 1.46, -0.25, 1.18, 0, 'glass'); cabin.rotation.z = -0.05;
      box(1.55, 0.13, 1.52, -0.3, 1.54, 0, 'body');
      [-0.73, 0.73].forEach(z => {
        box(0.13, 0.66, 0.05, -0.32, 1.18, z, 'body');
        box(0.12, 0.66, 0.05, -1.1, 1.18, z, 'body');
        box(0.12, 0.66, 0.05, 0.57, 1.18, z, 'body');
        box(0.78, 0.34, 0.03, -0.2, 0.72, z * 1.2, 'white');
        box(0.32, 0.12, 0.22, 0.68, 1.06, z * 1.3, 'dark');
      });
      box(1.15, 0.03, 0.32, 1.28, 0.95, 0, 'dark');
      box(0.09, 0.23, 1.25, 1.98, 0.63, 0, 'dark');
      [-0.6, 0.6].forEach(z => { box(0.1, 0.18, 0.38, 2.02, 0.83, z, 'light'); box(0.1, 0.16, 0.3, -2, 0.8, z, 'red'); });
      [-0.54, 0.54].forEach(z => box(0.12, 0.32, 0.12, -1.7, 1.06, z, 'dark'));
      box(0.48, 0.1, 2, -1.72, 1.23, 0, 'dark');
      box(0.34, 0.08, 0.3, -0.5, 1.64, 0, 'dark');
      const wheels = [];
      [-1.22, 1.22].forEach(x => [-0.91, 0.91].forEach(z => {
        const wheel = new T.Group(); wheel.position.set(x, 0.4, z); car.add(wheel); wheels.push(wheel);
        const tire = new T.Mesh(new T.CylinderGeometry(0.43, 0.43, 0.3, 20), materials.dark); tire.rotation.x = Math.PI / 2; tire.castShadow = true; wheel.add(tire);
        const hub = new T.Mesh(new T.CylinderGeometry(0.24, 0.24, 0.315, 12), materials.rim); hub.rotation.x = Math.PI / 2; wheel.add(hub);
        for (let i = 0; i < 5; i++) { const spoke = box(0.38, 0.045, 0.32, 0, 0, 0, 'dark', wheel); spoke.rotation.z = i * Math.PI / 5; }
      }));
      const floor = new T.Mesh(new T.PlaneGeometry(200, 200), new T.ShadowMaterial({ opacity: 0.3 })); floor.rotation.x = -Math.PI / 2; floor.receiveShadow = true; scene.add(floor);
      const ring = new T.Mesh(new T.RingGeometry(2.6, 2.63, 80), new T.MeshBasicMaterial({ color: 0x71806a, transparent: true, opacity: 0.4, side: T.DoubleSide })); ring.rotation.x = -Math.PI / 2; ring.position.y = 0.01; scene.add(ring);
      let target = 0, frame, visible = true, time = 0, last = performance.now();
      const resize = () => { const { width, height } = el.getBoundingClientRect(); renderer.setSize(width, height); camera.aspect = width / Math.max(height, 1); camera.updateProjectionMatrix(); };
      const observer = new ResizeObserver(resize); observer.observe(el);
      const intersection = new IntersectionObserver(([entry]) => { visible = entry.isIntersecting; }); intersection.observe(el);
      const pointer = e => { if (!pause.current) target = (e.clientX - el.getBoundingClientRect().left) / el.clientWidth - 0.5; };
      el.addEventListener('pointermove', pointer);
      const animate = now => {
        const dt = Math.min((now - last) / 1000, 0.05); last = now;
        if (visible && !document.hidden) {
          if (!pause.current) { time += dt; car.rotation.y += ((Math.sin(time * 0.28) * 0.22 + target * 0.65) - car.rotation.y) * 0.035; car.position.y = Math.sin(time * 3) * 0.015; wheels.forEach(w => { w.rotation.z -= dt * 1.5; }); }
          renderer.render(scene, camera);
          renderer.domElement.dataset.rendered = 'true';
        }
        frame = requestAnimationFrame(animate);
      };
      resize(); frame = requestAnimationFrame(animate);
      dispose = () => { cancelAnimationFrame(frame); observer.disconnect(); intersection.disconnect(); el.removeEventListener('pointermove', pointer); scene.traverse(obj => obj.geometry?.dispose()); Object.values(materials).forEach(m => m.dispose()); floor.material.dispose(); ring.material.dispose(); renderer.dispose(); renderer.domElement.remove(); };
    }).catch(() => setFailed(true));
    return () => { cancelled = true; dispose?.(); };
  }, []);
  return <div className="rally-scene"><div ref={host} className="rally-canvas" role="img" aria-label="Interactive lime green three-dimensional rally car" />{failed && <div className="rally-fallback">STOCKLINE<br /><b>RALLY / 01</b></div>}<div className="rally-caption"><span>RALLY / 01 · MOVE TO EXPLORE</span><button onClick={() => setPaused(p => !p)} aria-pressed={paused}>{paused ? 'Play motion' : 'Pause motion'}</button></div></div>;
}
